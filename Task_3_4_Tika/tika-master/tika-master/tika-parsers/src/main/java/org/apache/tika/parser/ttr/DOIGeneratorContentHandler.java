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

import org.apache.commons.codec.binary.Base64;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

public class DOIGeneratorContentHandler extends ContentHandlerDecorator {
    private Metadata metadata;
    private static String DOI = "doi";
    private static List<String> metaList = Arrays.asList("title","resourceName","author","description","og:url");

    public DOIGeneratorContentHandler(ContentHandler handler, Metadata metadata) {
        super(handler);
        this.metadata = metadata;
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        String doi = GenerateDOIFromMetadata(metadata);
        metadata.add(DOI, doi);
    }

    private static String GenerateDOIFromMetadata(Metadata metadata){
        String encodedStr="";
        StringBuilder metaString=new StringBuilder();
        String[] metadataNames = metadata.names();

        for(String name : metadataNames){
            if(metaList.contains(name))
                metaString.append(metadata.get(name));
        }

        try {
            encodedStr = Base64.encodeBase64URLSafeString(String.valueOf(metaString).getBytes("UTF-8"));
        }
        catch(UnsupportedEncodingException  e){
            System.out.println("Encoding ERROR::: "+e.getMessage());
        }
        return encodedStr;
    }
}
