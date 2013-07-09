package bakeparser;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import bakeparser.BakeNodeManager.BakeNode;


public class BakeParser {
	
	/*
	 * @author Apurv Kiri
	 * 
	 * README
	 * - Bakeparser supports as needed parsing
	 * - Provide tag to parse as "parent>child1>child2" provide object and object's method which must have string argument
	 * - Parser will bake object for you.
	 */
	public static BakeParser newInstance()
	{
		return new BakeParser();
	}
	/*
	 * No public constructor
	 */
	private BakeParser(){}
	
	public static interface BakeParserListener
	{
		public BakeParserRequestBuilder buildRequests();
		public InputStream bakeParserStream();
		public void onBakingCompleted();
		public InputSource bakeParserSource();
	}
	
	public BakeParserRequestBuilder newRequestBuilder()
	{
		return new BakeParserRequestBuilder();
	}
	
	public void registerListener(BakeParserListener listener) throws ParserConfigurationException, SAXException, IOException
	{
		Baker baker = new Baker(listener.buildRequests(),listener);
		
		SAXParserFactory fact = SAXParserFactory.newInstance();
		SAXParser parser = fact.newSAXParser();
		InputStream is = listener.bakeParserStream();
		if(is!=null)
		parser.parse(listener.bakeParserStream(), baker);
		else
		parser.parse(listener.bakeParserSource(), baker);
	}
	
		
		private static class Baker extends DefaultHandler
		{

			
			private String currentTag="",tempTag="";
			private BakeNodeManager manager;
			private BakeParserListener listener;
			private BakeNode currentNode;
			public Baker(BakeParserRequestBuilder requests, BakeParserListener listener)
			{
				
				this.manager = requests.getRequests();
				this.listener = listener;
			}
			
			private String currentContent="";

			@Override
			public void startElement(String arg0, String localName, String qName,
					Attributes attrs) throws SAXException {
				tempTag = currentTag;
				
					if(currentTag.equals(""))
					{
						currentTag=qName;
					}
					else
					{
						currentTag+=">"+qName;
					}
				
				
				currentNode = manager.findNode(currentTag);
				
				if(currentNode!=null)
				{
					currentNode.callStartTagMethod();
					for(int i=0; i<attrs.getLength(); i++)
					{
						
						currentNode.callParameterMethod(attrs.getQName(i), attrs.getValue(i));
					}	
				}
				else
				{
					currentNode = manager.findNode(tempTag+">*");
					if(currentNode!=null)
					{
						currentNode.callStartTagMethod(qName);
						for(int i=0; i<attrs.getLength(); i++)
						{
							
							currentNode.callParameterMethod(attrs.getQName(i), attrs.getValue(i),qName);
							
							
						}
					}
				}
				
			}
			@Override
			public void characters(char[] arg0, int arg1, int arg2)
					throws SAXException {
				if(currentNode!=null)
				currentContent+=(new String(arg0).substring(arg1, arg1+arg2));
			}

			
			private String exp[];
			
			@Override
			public void endElement(String arg0, String localName, String qName)
					throws SAXException {
				
				
				
				currentNode = manager.findNode(currentTag);
					/*
					 * Remove tag
					 */
					exp = null;
					exp = currentTag.split(">");
					if(exp.length!=0)
					{
						currentTag=exp[0];
						for(int i=1; i< exp.length-1; i++)
						{
							currentTag+=">"+exp[i];
							
						}
					}
					
						/*
						 * Calling for current tag
						 */
						if(currentNode!=null)
						{
							currentNode.callContentMethod(currentContent.trim());
							currentNode.callEndTagMethod();
						}
						else
						{
							currentNode = manager.findNode(currentTag+">*");
							if(currentNode!=null)
							{
								currentNode.callContentMethod(currentContent.trim(),qName);
								currentNode.callEndTagMethod(qName);
							}
						}
						currentContent="";
						currentNode = null;
						
						
				
			}
			
			@Override
			public void endDocument() throws SAXException {
				
				listener.onBakingCompleted();
				
			}	
			
		}
		
		
		
	
	

}
