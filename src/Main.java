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
import bakeparser.BakeParserRequestBuilder;


public class Main {
	
	private static NasaPodcasts podcast;
	private static BakeParser parser;
	public static void main(String arg[])
	{
		parser = BakeParser.newInstance();
		podcast = new NasaPodcasts();
		try {
			parser.registerListener(new BakeParserListener(){

				@Override
				public BakeParserRequestBuilder buildRequests() {
					BakeParserRequestBuilder builder = parser.newRequestBuilder();
					
					builder.addRequest("channel>title", podcast, null,null,"setTitle", null, null);
					builder.addRequest("channel>link", podcast, null, "setLink", null, null);
					builder.addRequest("item", podcast, "newItem", null, null, null);
					builder.addRequest("item>title", podcast, "getCurrentItem", null, "setTitle", null, null);
					builder.addRequest("item>link", podcast, "getCurrentItem", null, "setLink", null, null);
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

	public static class NasaPodcasts
	{
		private String title,link;
		
		public void setTitle(String s)
		{
			System.out.println("Title : "+s);
		}
		public void setLink(String s)
		{
			System.out.println("Link : "+s);
		}
		
		private Item currentItem;
		public Item getCurrentItem()
		{
			return currentItem;
		}
		public void newItem()
		{
			System.out.println("-- Item");
			currentItem = new Item();
		}
		public static class Item
		{
			private String title, link;
			
			public void setTitle(String s)
			{
				System.out.println("Title : "+s);
			}
			public void setLink(String s)
			{
				System.out.println("Link : "+s);
			}
			
		}
	}
}
