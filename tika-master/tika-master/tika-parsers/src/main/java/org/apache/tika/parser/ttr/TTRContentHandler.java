/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tika.parser.ttr;

import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.*;

/**
 * Class used to extract text based on text to tag ratio algo.
 *
 * Every time a document is parsed in Tika, the content is split into SAX events.
 * Those SAX events are handled by a ContentHandler. You can think of these events
 * as marking a tag in an HTML file. Once you're finished parsing, you can call
 * handler.toString(), for example, to get the text contents of the file. On the other
 * hand, any of the metadata of the file will be added to the Metadata object passed
 * in during the parse() call.  So, the Parser class sends metadata to the Metadata
 * object and content to the ContentHandler.
 *
 * This class is an example of how to combine a ContentHandler and a Metadata.
 *
 * Please see the TTRContentHandlerTest for an example of how to use
 * this class.
 *
 */
public class TTRContentHandler extends ContentHandlerDecorator {
    private static final int THRESHOLD = 20;
    private int wait = 0;
    private List<Integer> binTokens = new ArrayList<>();
    private List<String> tokens = new ArrayList<>();

    public TTRContentHandler(ContentHandler handler) {
        super(handler);
    }

    protected TTRContentHandler() {
        this(new BodyContentHandler());
    }

    // we ignore all the below tags
    private  static HashSet ignoreTags = new HashSet(Arrays.asList("script", "link" , "head","style","comment") );
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        // we ignore all the below tags
        if (ignoreTags.contains(localName)) {
            wait++;
        }else if (wait == 0){
            binTokens.add(1);
            tokens.add(null);
            super.startElement(uri, localName, qName, atts);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        // ignore the elements which are inside of the tags we are ignoring
        if(wait > 0) return;

        String text = new String(Arrays.copyOfRange(ch, start, start + length)).trim();
        if(text.length()> 0) {
            for (String s : text.split(" ")) {
                binTokens.add(0);
                tokens.add(s);
            }
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
    }

    // for every end tag record it as a tag
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ignoreTags.contains(localName)) {
            wait--;
        }else if (wait == 0){
            binTokens.add(1);
            tokens.add(null);
            if(localName.equals("body")){
                // at this point we need to write the get the ttr text we generated and write to parent handler
                String text = getTTRTextAndWriteToHandler();
                // send the read characters to downstream handlers
                super.characters(text.toCharArray(),0,text.length());
            }
            super.endElement(uri, localName, qName);
        }
    }

    public String  getTTRTextAndWriteToHandler() throws SAXException {

        StringBuilder stringBuilder = new StringBuilder();

        // transform [0 1 0 1 1 0 ] => [0 1 1 2 3 3]
        for (int i = 1 ; i<binTokens.size(); i++) {
            binTokens.set(i,  binTokens.get(i)+ binTokens.get(i-1));
        }

        // now find out the  sub arrays with maximum character tokens
        int len = binTokens.size();
        List<Chunk> chunks = new ArrayList<>();
        Chunk currentMaxChunk = null;
        for (int i = 0 ; i<len; i++) {
            for (int j = i+1 ; j<len; j++) {
                int tagAfter = binTokens.get(len-1) - binTokens.get(j);
                int tagBefore = binTokens.get(i);
                int textBetween= (j - i) - (binTokens.get(j) - binTokens.get(i));
                int val = tagAfter + tagBefore + textBetween;

                if( currentMaxChunk == null ){
                    currentMaxChunk = new Chunk(i, j,val);
                } // check if were in the same overlap as previous and have greater max
                // then update the current max
                else if( i <= currentMaxChunk.j &&  val > currentMaxChunk.val ){
                    currentMaxChunk.Update(i, j,val);
                }// if we are not overlapping the previous max tuple
                // put the previous one in treeMap
                else if(currentMaxChunk!= null &&  i > currentMaxChunk.j){
                    chunks.add(currentMaxChunk);
                    currentMaxChunk = new Chunk(i, j,val);
                }
            }
        }

        if(currentMaxChunk!=null)chunks.add(currentMaxChunk);
        Collections.sort(chunks);

        for (int i = 0 ; i< Math.min( chunks.size(), THRESHOLD ); i++) {
            Chunk c = chunks.get(i);
            for (int j = c.i ; j<=c.j; j++) {
                String s = tokens.get(j);
                if(s != null)
                stringBuilder.append(s).append(" ");
            }
            // need to remove this line after debugging
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();

    }

    public class Chunk implements Comparable<Chunk> {
        public int i, j, val;
        public Chunk(int i, int j, int val) {
            this.i = i;
            this.j = j;
            this.val = val;
        }

        public void Update(int i, int j, int val) {
            this.i = i;
            this.j = j;
            this.val = val;
        }

        @Override
        public int compareTo(Chunk o) {
            return ( o.val - val);
        }
    }
}
