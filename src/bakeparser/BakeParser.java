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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import bakeparser.BakeParser.BakeParserRequestBuilder.BakeParserRequest;

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
		
	}
	
	public BakeParserRequestBuilder newRequestBuilder(String startTag)
	{
		return new BakeParserRequestBuilder(startTag);
	}
	public static class BakeParserRequestBuilder
	{
		/*
		 * Parent tag has to be mentioned.
		 */
		private String startTag;
		private LinkedList<BakeParserRequest> requests;
		public BakeParserRequestBuilder(String startTag)
		{
			this.startTag = startTag;
			requests = new LinkedList<BakeParserRequest>();
		}
		/*
		 * When <tag> is started, callObject's startMethodName is called, for content contentMethodName is called, for parameter's of the tag, parameterMethodName is called
		 * for end of tag, endTagMethod is called.
		 * 
		 * ..................
		 * tagName has to be in format of parent>child1>child2>child3, skipping any of the hierarchy will not parse that tag.
		 * 
		 * startTag method has no parameters
		 * contentMethod has one String parameter
		 * parameter method has two string parameters as key,value
		 * endTag method has no parameters
		 */
		public void addRequest(String tagName, Object callObject,String startMethodName, String contentMethodName,String parameterMethodName,String endTagMethod)
		{
			requests.add(new BakeParserRequest(tagName, callObject,startMethodName ,contentMethodName, parameterMethodName,endTagMethod));
		}
		public void addRequest(String tagName, Object callObject, String callObjectGetter,String startMethodName, String contentMethodName,String parameterMethodName,String endTagMethod)
		{
			requests.add(new BakeParserRequest(tagName, callObject, callObjectGetter ,startMethodName ,contentMethodName, parameterMethodName,endTagMethod));
		}
		public void setStartTag(String startTag)
		{
			this.startTag = startTag;
		}
		
		public String getStartTag()
		{
			return startTag;
		}
		public LinkedList<BakeParserRequest> getRequests()
		{
			return requests;
		}
		
		public static class BakeParserRequest
		{
			private String tagName,contentMethodName,parameterMethodName,endTagMethod,startTagMethod,objectGetter;
			private Object callObject;
			private boolean objectGetterMode = false;
			public BakeParserRequest(String tagName, Object callObject,String startTagMethod,String contentMethodName,String parameterMethodName,String endTagMethod)
			{
				this.tagName = tagName;
				this.callObject = callObject;
				this.contentMethodName = contentMethodName;
				this.parameterMethodName = parameterMethodName;
				this.endTagMethod = endTagMethod;
				this.startTagMethod = startTagMethod;
			}
			public BakeParserRequest(String tagName,Object callObject,String objectGetter,String startTagMethod,String contentMethodName,String parameterMethodName,String endTagMethod)
			{
				this.tagName = tagName;
				this.callObject = callObject;
				this.objectGetter = objectGetter;
				objectGetterMode = true;
				this.contentMethodName = contentMethodName;
				this.parameterMethodName = parameterMethodName;
				this.endTagMethod = endTagMethod;
				this.startTagMethod = startTagMethod;
			}
			
			/*
			 * Useful getter setters
			 */
			public String getTagName()
			{
				return tagName;
			}
			
			/*
			 * Useless getter setters
			 */
			public String getContentMethodName()
			{
				return contentMethodName;
			}
			public String getParameterMethodName()
			{
				return parameterMethodName;
			}
			
			/*
			 * Useful methods
			 */
			public void callContentMethod(String content)
			{
				
					if(contentMethodName!=null)
					{
						if(!objectGetterMode)
						{
							try {
								callObject.getClass().getMethod(contentMethodName, String.class).invoke(callObject, content);
							} catch (NoSuchMethodException | SecurityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						else
						{
							try {
								
								
								String methods[] = objectGetter.split(">");
								Object call = callObject.getClass().getMethod(methods[0],null).invoke(callObject, null);
								for(String s : methods)
								{
									call = call.getClass().getMethod(s, null).invoke(call, null);
								}
								call.getClass().getMethod(contentMethodName, String.class).invoke(callObject, content);
							} catch (NoSuchMethodException | SecurityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
				}
				
				
			}
			
			public void callParameterMethod(String key, String value)
			{
				
				if(parameterMethodName!=null)
				{
					if(!objectGetterMode)
					{
						try {
							callObject.getClass().getMethod(parameterMethodName, String.class, String.class).invoke(callObject, key,value);
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
					{
						try {
							String methods[] = objectGetter.split(">");
							Object call = callObject.getClass().getMethod(methods[0],null).invoke(callObject, null);
							for(String s : methods)
							{
								call = call.getClass().getMethod(s, null).invoke(call, null);
							}
							call.getClass().getMethod(objectGetter, null).invoke(callObject, null).getClass().getMethod(parameterMethodName, String.class, String.class).invoke(callObject, key,value);
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			
			public void callEndTagMethod()
			{
				
				if(endTagMethod!=null)
				{
					if(!objectGetterMode)
					{
						try {
							callObject.getClass().getMethod(endTagMethod, null).invoke(callObject, null);
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
					{
						try {
							String methods[] = objectGetter.split(">");
							Object call = callObject.getClass().getMethod(methods[0],null).invoke(callObject, null);
							for(String s : methods)
							{
								call = call.getClass().getMethod(s, null).invoke(call, null);
							}
							call.getClass().getMethod(objectGetter, null).invoke(callObject, null).getClass().getMethod(endTagMethod, null).invoke(callObject, null);
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			public void callStartTagMethod()
			{
				if(startTagMethod!=null)
				{
					if(!objectGetterMode)
					{
						try {
							callObject.getClass().getMethod(startTagMethod, null).invoke(callObject, null);
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
					{
						try {
							String methods[] = objectGetter.split(">");
							Object call = callObject.getClass().getMethod(methods[0],null).invoke(callObject, null);
							for(String s : methods)
							{
								call = call.getClass().getMethod(s, null).invoke(call, null);
							}
							call.getClass().getMethod(objectGetter, null).invoke(callObject, null).getClass().getMethod(startTagMethod, null).invoke(callObject, null);
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
				
			}
			
			
		}
		
	}
	public void registerListener(BakeParserListener listener) throws ParserConfigurationException, SAXException, IOException
	{
		Baker baker = new Baker(listener.buildRequests(),listener);
		
		SAXParserFactory fact = SAXParserFactory.newInstance();
		SAXParser parser = fact.newSAXParser();
		parser.parse(listener.bakeParserStream(), baker);
		
	}
	
		
		private static class Baker extends DefaultHandler
		{

			private Hashtable<String,BakeParserRequest> requests;
			private String currentTag="",startTag="";
			private BakeParserRequest currentRequest;
			private BakeParserListener listener;
			
			public Baker(BakeParserRequestBuilder requests, BakeParserListener listener)
			{
				startTag = requests.getStartTag();
				this.requests = new Hashtable<String,BakeParserRequest>();
				for(BakeParserRequest req : requests.getRequests())
				{
					this.requests.put(req.getTagName(),req );
				}
				
				this.listener = listener;
			}
			
			private String currentContent="";

			@Override
			public void startElement(String arg0, String localName, String qName,
					Attributes attrs) throws SAXException {
				if(qName.equals(startTag))
				{
					if(currentTag.equals(""))
					{
						currentTag=startTag;
					}
					else
					{
						currentTag+=">"+qName;
					}
				}
				else if(!currentTag.equals(""))
				{
					currentTag+=">"+qName;
				}
				
				currentRequest = requests.get(currentTag);
				
				if(currentRequest!=null)
				{
					
					currentRequest.callStartTagMethod();
					for(int i=0; i<attrs.getLength(); i++)
					{
						
						currentRequest.callParameterMethod(attrs.getQName(i), attrs.getValue(i));
						
						
					}
					
					
				}
				
			}
			@Override
			public void characters(char[] arg0, int arg1, int arg2)
					throws SAXException {
				currentContent+=(new String(arg0).substring(arg1, arg1+arg2));
			}

			
			private String exp[];
			
			@Override
			public void endElement(String arg0, String localName, String qName)
					throws SAXException {
				
				
				
					currentRequest = requests.get(currentTag);
					
						/*
						 * Calling for current tag
						 */
						if(currentRequest!=null)
						{currentRequest.callContentMethod(currentContent.trim());
						currentRequest.callEndTagMethod();}
						currentContent="";
						currentRequest = null;
						
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
				
			}
			
			@Override
			public void endDocument() throws SAXException {
				
				listener.onBakingCompleted();
				
			}	
			
		}
		
		
		
	
	

}
