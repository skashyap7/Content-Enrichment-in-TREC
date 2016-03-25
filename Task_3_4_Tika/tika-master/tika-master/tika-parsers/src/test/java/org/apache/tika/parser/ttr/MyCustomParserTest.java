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
//import org.apache.commons.io.FilenameUtils;
//import org.apache.tika.Tika;
//import org.apache.tika.metadata.Metadata;
//import org.apache.tika.metadata.TikaCoreProperties;
//import org.apache.tika.parser.AutoDetectParser;
//import org.apache.tika.parser.Parser;
//import org.junit.Test;
//
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.InputStream;
//
//import static java.nio.charset.StandardCharsets.UTF_8;
//import static org.apache.tika.TikaTest.assertContains;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//public class MyCustomParserTest {
//
//    @Test
//    public void testBodyContentHandler() throws Exception {
//        String test = "\n" +
//                "<html>\n" +
//                "  <body>\n" +
//                "    <center>\n" +
//                "      <p><img src=\"tika.png\" width=\"292\" height=\"100\"></p>\n" +
//                "\n" +
//                "      <p>Apache Tika&trade; ${project.version} <br> <br> is a toolkit for\n" +
//                "      detecting and extracting metadata and structured text content\n" +
//                "      from various documents using aries.</p>\n" +
//                "\n" +
//                "    This is a test document with a list of phone numbers:\n" +
//                "1. 9498888888\n" +
//                "2. 9497777777\n" +
//                "3. (949) 666 - 6666\n" +
//                "4. ninefourninefive5fivefive5fivefive\n" +
//                "5. 4-1-9-3-4-0-4-6-4-5\n" +
//                "6. [9][0][4]  [4][6][8]  [7][0][8][1]\n" +
//                "7. TwO/6/zERo-FouR/0/NiNe-4/eIgHt/1/One" +
//                "  <p><a href=\"/META-INF/NOTICE\">Copyright notice</a>\n" +
//                "   <p><a href=\"/META-INF/NOTICE\"></a>\n" +
//                "   <p><a href=\"/META-INF/NOTICE\"></a>\n" +
//                "   <p><a href=\"/META-INF/NOTICE\"></a>\n" +
//                "\n" +
//                "\n" +
//                "    </center>\n" +
//                "  </body>\n" +
//                "</html>\n";
//        Metadata metadata = new Metadata();
//        String TTRText =  new Tika().parseToString(new ByteArrayInputStream(test.getBytes(UTF_8)), metadata);
//
//        //Expecting first title to be set in meta data and second one to be ignored.
//        assertEquals("Simple Content", metadata.get(TikaCoreProperties.TITLE));
//    }
//
//    @Test
//    public void testPdfParsing() throws Exception {
//        Parser parser = new AutoDetectParser(); // Should auto-detect!
//
//        InputStream stream = MyCustomParser.class.getResourceAsStream(
//                "/test-documents/testPDF.pdf");
//        Metadata metadata = new Metadata();
//        String content =  new Tika().parseToString(stream, metadata);
//
//        assertEquals("application/pdf", metadata.get(Metadata.CONTENT_TYPE));
//        assertEquals("Bertrand Delacr\u00e9taz", metadata.get(TikaCoreProperties.CREATOR));
//        assertEquals("Bertrand Delacr\u00e9taz", metadata.get(Metadata.AUTHOR));
//        assertEquals("Firefox", metadata.get(TikaCoreProperties.CREATOR_TOOL));
//        assertEquals("Apache Tika - Apache Tika", metadata.get(TikaCoreProperties.TITLE));
//
//        // Can't reliably test dates yet - see TIKA-451
////        assertEquals("Sat Sep 15 10:02:31 BST 2007", metadata.get(Metadata.CREATION_DATE));
////        assertEquals("Sat Sep 15 10:02:31 BST 2007", metadata.get(Metadata.LAST_MODIFIED));
//
//        assertContains("Apache Tika", content);
//        assertContains("Tika - Content Analysis Toolkit", content);
//        assertContains("incubator", content);
//        assertContains("Apache Software Foundation", content);
//        // testing how the end of one paragraph is separated from start of the next one
//        assertTrue("should have word boundary after headline",
//                !content.contains("ToolkitApache"));
//        assertTrue("should have word boundary between paragraphs",
//                !content.contains("libraries.Apache"));
//    }
//
//    @Test
//    public void testMeasurements() throws Exception {
//        File file=new File("/test-documents/testHTML_measure.html");
//        InputStream stream = MyCustomParser.class.getResourceAsStream("/test-documents/testHTML_measure.html");
//        String basename = FilenameUtils.getBaseName(file.getPath());
//        Metadata metadata = new Metadata();
//        metadata.set(Metadata.RESOURCE_NAME_KEY, basename);
//        String content =  new Tika().parseToString(stream, metadata);
//        String[] metadataNames = metadata.names();
//        for(String name : metadataNames)
//            System.out.println(name+": "+metadata.get(name));
//    }
//}
