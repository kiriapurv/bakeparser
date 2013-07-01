Bake Parser

About 

- Bake Parser works as object's method caller for XML tags
- It uses SAX Parser

Working


- Bakeparser supports as needed parsing
- Provide tag to parse as "parent>child1>child2" provide object and object's method which must have string argument
- Parser will bake object for you.

Object's methods
When <tag> is started, callObject's startMethodName is called, for content contentMethodName is called, for parameter's of the tag, parameterMethodName is called for end of tag, endTagMethod is called.

tagName has to be in format of parent>child1>child2>child3, skipping any of the hierarchy will not parse that tag. 

startTag method has no parameters
contentMethod has one String parameter
parameter method has two string parameters as key,value
endTag method has no parameters

TODO
- Provide unique hierarchy identification, rather than using full hierarchy ( useful for too many parents and childs)
- Performance Improvements
- Unknown stuffs and bugs