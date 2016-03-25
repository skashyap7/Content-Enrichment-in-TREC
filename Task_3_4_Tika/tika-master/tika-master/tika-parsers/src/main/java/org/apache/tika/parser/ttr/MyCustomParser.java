///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.apache.tika.parser.ttr;
//
//import org.apache.tika.exception.TikaException;
//import org.apache.tika.metadata.Metadata;
//import org.apache.tika.mime.MediaType;
//import org.apache.tika.parser.AbstractParser;
//import org.apache.tika.parser.ParseContext;
//import org.apache.tika.parser.Parser;
//import org.apache.tika.parser.html.HtmlParser;
//import org.apache.tika.parser.pdf.PDFParser;
//import org.xml.sax.ContentHandler;
//import org.xml.sax.SAXException;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * Custom parser to parse html and PDF .
// */
//public class MyCustomParser extends AbstractParser {
//
//
//    private static final MediaType XHTML = MediaType.application("xhtml+xml");
//    private static final MediaType WAP_XHTML = MediaType.application("vnd.wap.xhtml+xml");
//    private static final MediaType X_ASP = MediaType.application("x-asp");
//    private static final MediaType PDF = MediaType.application("pdf");
//
//    private static final Set<MediaType> SUPPORTED_TYPES =
//            Collections.unmodifiableSet(new HashSet<MediaType>(Arrays.asList(
//                    MediaType.text("html"),
//                    XHTML,
//                    WAP_XHTML,
//                    X_ASP,
//                    PDF)));
//
//
//    public Set<MediaType> getSupportedTypes(ParseContext context) {
//        return SUPPORTED_TYPES;
//    }
//
//    public void parse(
//            InputStream stream, ContentHandler handler,
//            Metadata metadata, ParseContext context)
//            throws IOException, SAXException, TikaException {
//
//        // get the ttr first
//        TTRContentHandler ttrContentHandler = new TTRContentHandler(handler);
//
//        // extract measurements and add to metadata
//        MeasurementExtractingContentHandler measurementExtractingContentHandler =
//                new MeasurementExtractingContentHandler(ttrContentHandler, metadata);
//
//        // generate DOI and add to metadata
//        DOIGeneratorContentHandler doiGeneratorContentHandler =
//                new DOIGeneratorContentHandler(measurementExtractingContentHandler, metadata);
//
//        Parser parser =  "application/pdf".equals(metadata.get(Metadata.CONTENT_TYPE)) ? new PDFParser(): new HtmlParser();
//        parser.parse(stream, doiGeneratorContentHandler, metadata, context);
//    }
//}
