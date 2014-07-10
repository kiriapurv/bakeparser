package bakeparser;

import java.util.Hashtable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import bakeparser.BakeParser.BakeParserListener;
import bakeparser.BakeParser.BakeParserRequestBuilder;
import bakeparser.BakeParser.BakeParserRequestBuilder.BakeParserRequest;

public class Baker extends DefaultHandler {

	private Hashtable<String,BakeParserRequest> requests;
	private BakeParserRequest currentRequest;
	private BakeParserListener listener;
	private StringBuilder sResponse, currentContent;
	private boolean isInitiated = false;
	private TagStack tagStack;
	private String startTag, currentTag;
	
	public Baker(BakeParserRequestBuilder requests, BakeParserListener listener)
	{
		sResponse = new StringBuilder();
		currentContent = new StringBuilder();
		tagStack = new TagStack();
		startTag = requests.getStartTag();
		this.requests = new Hashtable<String,BakeParserRequest>();
		for(BakeParserRequest req : requests.getRequests()) {
			this.requests.put(req.getTagName(),req );
		}
		this.listener = listener;
	}

	@Override
	public void startElement(String arg0, String localName, String qName,
			Attributes attrs) throws SAXException {
		
		sResponse.append("<").append(qName);
		
		if(qName.equals(startTag) && !isInitiated) {
			tagStack.push(startTag);
			isInitiated = true;
		}
		else if(isInitiated) {
			tagStack.push(qName);
		}
		
		currentTag = tagStack.getHierarchy();
		System.out.println("Current Tag : "+currentTag);
		if(requests.containsKey(currentTag)) {
			currentRequest = requests.get(currentTag);
			currentRequest.callStartTagMethod();
			for(int i=0; i<attrs.getLength(); i++) {
				sResponse.append(" ").append(attrs.getQName(i)).append("=\"").append(attrs.getValue(i)).append("\"");
				currentRequest.callParameterMethod(attrs.getQName(i), attrs.getValue(i));
			}	
		}
		else if(requests.containsKey(tagStack.getWildCardHierarchy())) {
			
			currentRequest = requests.get(tagStack.restoreWildCard());
			currentRequest.callStartTagMethod(qName);
			for(int i=0; i<attrs.getLength(); i++) {
					sResponse.append(" ").append(attrs.getQName(i)).append("=\"").append(attrs.getValue(i)).append("\"");
					currentRequest.callParameterMethod(attrs.getQName(i), attrs.getValue(i),qName);
			}
		}
		tagStack.restoreWildCard();
		sResponse.append(">");
		
	}
	

	@Override
	public void characters(char[] arg0, int arg1, int arg2)
			throws SAXException {
		String resp = (new String(arg0).substring(arg1, arg1+arg2));
		sResponse.append(resp);
		if(currentRequest!=null) {
			currentContent.append(resp);
		}
	}
	
	@Override
	public void endElement(String arg0, String localName, String qName)
			throws SAXException {
		
			sResponse.append("</").append(qName).append(">");
			currentRequest = requests.get(currentTag);
			/*
			 * Remove tag
			 */
			if(!tagStack.isEmpty()) {
				tagStack.pop();
			
				/*
				 * Calling for current tag
				 */
				if(currentRequest!=null) {
					if(!currentContent.toString().trim().equals(""))
						currentRequest.callContentMethod(currentContent.toString().trim());
					currentRequest.callEndTagMethod();
				}
				else {
					currentRequest = requests.get(tagStack.getWildCardHierarchy());
					if(currentRequest!=null) {
						if(!currentContent.toString().trim().equals(""))
							currentRequest.callContentMethod(currentContent.toString().trim(),qName);
						currentRequest.callEndTagMethod(qName);
					}
				}
				tagStack.restoreWildCard();
				currentContent.setLength(0);
				currentRequest = null;
			}
	}
	
	@Override
	public void endDocument() throws SAXException {
		if(listener!=null)
			listener.onBakingCompleted(sResponse.toString());
	}	
}
