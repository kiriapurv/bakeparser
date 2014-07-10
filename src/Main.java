import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import bakeparser.BakeParser;
import bakeparser.BakeParser.BakeParserListener;
import bakeparser.BakeParser.BakeParserRequestBuilder;


public class Main {
	
	private static NasaPodcasts podcast;
	private static BakeParser parser;
	public static void main(String arg[])
	{
		Thread tr = new Thread(new Runnable(){
			public void run()
			{
				podcast = new NasaPodcasts();
				
				BakeParserRequestBuilder builder = BakeParser.newRequestBuilder("channel");
				
				/*
				 * For content of <title> tag call setTitle method of podcast object
				 */
				builder.addRequest("channel>title", podcast, null, "setTitle", null, null);
				/*
				 * For content of <link> tag call setLink method of podcast object
				 */
				builder.addRequest("channel>link", podcast, null, "setLink", null, null);
				/*
				 * For content of <description> tag call setDescription method of podcast object
				 */
				builder.addRequest("channel>description", podcast, null, "setDescription", null, null);
				
				/*
				 * For <item> tag when tag is started, call newItem method of podcast object, and when tag ends call addItem method of podcast object
				 */
				builder.addRequest("channel>item", podcast, "newItem", null, null,"addItem");
				
				//builder.addRequest("channel>item>*", podcast, null,"set*", null,null);
				/*
				 * For <title> tag under <item> invoke both methodOne and methodTwo when tag is started as well as when content is captured
				 */
				builder.addRequest("channel>item>title", podcast, "methodOne,methodTwo", "methodOne,methodTwo", null, null);
				/*
				 * For <link> tag under <item> tag, first call getOO method of podcast object, and whatever object is returned by it (say foo ), call setText method of object foo for content of link tag
				 */
				builder.addRequest("channel>item>link", podcast,"getOO" ,null,"setText", null,null);
				/*
				 * For <pubDate> tag first call getOO method of podcast ( which returns foo object ), then call getBB method of object foo (which returns bar object ) and then call setText method of bar object for content of pubDate
				 */
				builder.addRequest("channel>item>pubDate", podcast,"getOO>getBB" ,null,"setText", null,null);
				
				builder.addRequest("channel>item>description", podcast, null,"setItemDescription", null,null);
				
				/*
				 * For parameters of <enclosure> tag, if "url" parameter is captured then parameterTest method will be called, for every else parameter setItemEnclosure method will be called
				 */
				builder.addRequest("channel>item>enclosure", podcast, "startItemEnclosure",null, "url>parameterTest|*>setItemEnclosure","closeItemEnclosure");
				
				parser = BakeParser.newInstance(builder);
				
				parser.setListener(new BakeParserListener() {

					@Override
					public void onBakingCompleted(String response) {
						System.out.println(response);
					}
					
				});
				
				try {
					parser.parse(new URL("http://science1.nasa.gov/media/medialibrary/2010/12/09/podcast__.xml").openStream());
				} catch (SAXException | IOException
						| ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		tr.start();
	}

	public static class NasaPodcasts
	{
		public void methodOne()
		{
			p("Method 1");
		}
		public void methodTwo()
		{
			p("Method 2");
		}
		public void methodOne(String s)
		{
			p("Method 1 : "+s);
		}
		public void methodTwo(String s)
		{
			p("Method 2 : "+s);
		}
		public OO getOO()
		{
			return new OO();
		}
		public void settitle(String str)
		{
			p("Wildcard -> "+str);
		}
		public static class OO
		{
			public void setText(String text)
			{
				
				p("OO --- > "+text);
			}
			
			public BB getBB()
			{
				
				return new BB();
			}
			public static class BB
			{
				public void setText(String text)
				{
					p("BB --- > "+text);
				}
			}
		}
		private String title,link,description;
		private Item currentItem;
		private LinkedList<Item> items;
		public NasaPodcasts()
		{
			items = new LinkedList<Item>();
		}
		
		public void setTitle(String title) {
			p("Title : "+title);
			this.title = title;
		}

		public void setLink(String link) {
			p("Link : "+link);
			this.link = link;
		}

		public void setDescription(String description) {
			p("Description : "+description);
			this.description = description;
		}

		public static class Item
		{
			public String title,link,pubDate,description,enclosureUrl,enclosureLength,enclosureType;		
		}
		
		public void newItem()
		{
			p("--------- ITEM");
			currentItem =null;
			currentItem = new Item();
		}
		public void addItem()
		{
			p("///-------- ITEM");
			items.add(currentItem);
		}
		
		public void setItemTitle(String title) {
			p("Item Title : "+title);
			currentItem.title = title;
		}

		public void setItemLink(String link) {
			p("Item Link : "+link);
			currentItem.link = link;
		}

		public void setItemPubDate(String pubDate) {
			p("Item Pub Date : "+pubDate);
			currentItem.pubDate = pubDate;
		}

		public void setItemDescription(String description) {
			p("Item Description : "+description);
			currentItem.description = description;
		}

		public void startItemEnclosure()
		{
			p("Item Enclosure : --------------->");
		}
		public void parameterTest(String str)
		{
			p(":::>>>>>>>> Parameter Passed  : "+str);
		}
		public void setItemEnclosure(String key, String value)
		{
			
			
			p(">"+key+": "+value);
			if(key.equals("url"))
			{
				currentItem.enclosureUrl = value;
			}
			else if(key.equals("length"))
			{
				currentItem.enclosureLength  = value;
			}
			else if(key.equals("type"))
			{
				currentItem.enclosureType=value;
			}
		}
		
		public void closeItemEnclosure()
		{
			p("//--------- Item Enclosure");
		}
		
		
		
		
	}
	
	private static void p(String s)
	{
		System.out.println(""+s);
	}
}
