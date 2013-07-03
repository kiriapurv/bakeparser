package bakeparser;

import java.util.LinkedList;



public class BakeParserRequestBuilder
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
	public BakeParserRequestBuilder addRequest(String tagName, Object callObject,String startMethodName, String contentMethodName,String parameterMethodName,String endTagMethod)
	{
		requests.add(new BakeParserRequest(tagName, callObject,startMethodName ,contentMethodName, parameterMethodName,endTagMethod));
		return this;
	}
	public BakeParserRequestBuilder addRequest(String tagName, Object callObject, String callObjectGetter,String startMethodName, String contentMethodName,String parameterMethodName,String endTagMethod)
	{
		requests.add(new BakeParserRequest(tagName, callObject, callObjectGetter ,startMethodName ,contentMethodName, parameterMethodName,endTagMethod));
		return this;
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
	
	
	
}
