import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
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
				parser = BakeParser.newInstance();
				podcast = new NasaPodcasts();
				try {
					parser.registerListener(new BakeParserListener(){

						@Override
						public BakeParserRequestBuilder buildRequests() {
							BakeParserRequestBuilder builder = parser.newRequestBuilder("channel");
							
							builder.addRequest("channel>title", podcast, null, "setTitle", null, null);
							builder.addRequest("channel>link", podcast, null, "setLink", null, null);
							builder.addRequest("channel>description", podcast, null, "setDescription", null, null);
							
							builder.addRequest("channel>item", podcast, "newItem", null, null,"addItem");
							/* Wildcard, will cause exceptions for unhandled tags */
							//builder.addRequest("channel>item>*", podcast, null,"set*", null,null);
							builder.addRequest("channel>item>title", podcast, "methodOne,methodTwo", "methodOne,methodTwo", null, null);
							
							builder.addRequest("channel>item>link", podcast,"getOO" ,null,"setText", null,null);
							builder.addRequest("channel>item>pubDate", podcast,"getOO>getBB" ,null,"setText", null,null);
							builder.addRequest("channel>item>description", podcast, null,"setItemDescription", null,null);
							builder.addRequest("channel>item>enclosure", podcast, "startItemEnclosure",null, "url>parameterTest|*>setItemEnclosure","closeItemEnclosure");
							return builder;
						}

						@Override
						public InputStream bakeParserStream() {
							URL url;
							try {
								url = new URL("http://science1.nasa.gov/media/medialibrary/2010/12/09/podcast__.xml");
								return url.openStream();
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return null;
						}

						@Override
						public void onBakingCompleted() {
							
							podcast.complete();
						}

						@Override
						public InputSource bakeParserSource() {
							// TODO Auto-generated method stub
							return null;
						}
						
					});
				} catch (ParserConfigurationException e) {
					
					e.printStackTrace();
				} catch (SAXException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
		});
		tr.start();
	}

	public static class NasaPodcasts
	{
		private void waitwait()
		{
			try
			{
				Thread.sleep(500);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		public void methodOne()
		{
			waitwait();
			p("Method 1");
		}
		public void methodTwo()
		{
			waitwait();
			p("Method 2");
		}
		public void methodOne(String s)
		{
			waitwait();
			p("Method 1 : "+s);
		}
		public void methodTwo(String s)
		{
			waitwait();
			p("Method 2 : "+s);
		}
		public OO getOO()
		{
			waitwait();
			return new OO();
		}
		public void settitle(String str)
		{
			waitwait();
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
		public void complete()
		{
			p("----- Completed --- : Added "+items.size()+" Items");
		}
		public void setTitle(String title) {
			waitwait();
			p("Title : "+title);
			this.title = title;
		}

		public void setLink(String link) {
			waitwait();
			p("Link : "+link);
			this.link = link;
		}

		public void setDescription(String description) {
			waitwait();
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
