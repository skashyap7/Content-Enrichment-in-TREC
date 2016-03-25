/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright owlocationNameEntitieship.
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
package org.apache.tika.parser.ner.regex;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ner.NamedEntityParser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexNERecogniserTest {

    @Test
    public void testGetEntityTypes() throws Exception {

        String text = "Hey, Lets meet on this Sunday or MONDAY because i am busy on Saturday. My number is 323-456-9808.";
        System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL, RegexNERecogniser.class.getName());

        Tika tika = new Tika(new TikaConfig(NamedEntityParser.class.getResourceAsStream("tika-config.xml")));
        Metadata md = new Metadata();
        tika.parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), md);

        Set<String> days = new HashSet<>(Arrays.asList(md.getValues("NER_WEEK_DAY")));
        Set<String> other = new HashSet<>(Arrays.asList(md.getValues("NER_PHONE_NUMBER")));
        assertTrue(days.contains("Sunday"));
        assertTrue(days.contains("MONDAY"));
        assertTrue(days.contains("Saturday"));
        assertTrue(days.size() == 3); //and nothing else

        TesterClass.getExtracted();
       }
}


class TesterClass {
    public static final String[] testArray =
            {
                    "a. Dynabrade 4\" Discs 34-333-102",
                    "b. Mercer 4 Inch Discs",
                    "c. Mercer 4in Discs",
                    "d. Carbo CleanAir 6\' Vacuum Tube Attachment",
                    "e. 4 feet",
                    "f. 4 Ft",
                    "g. 4 foot",
                    "h. Carborundum 2-3/4\" Tape 4 yd Roll 97580",
                    "i. I want 5 Inches and later 6 Feet",
                    "j. I don't want this one pyrex 9",
                    "k. This is 4 Inches x 5 Inches x 6 Feet in Size",
                    "l. This one is 6 x 5 Inches",
                    "m. This is 4\" x 5\" x 6\' in size",
                    "n. Something 4-3/4\" Long",
                    "o. I don't want 9 xtreme things",
                    "p. I don't want 9 men",
                    "q. 674m",
                    "r. 4 Inches",
                    "s. 5x8",
                    "t. P58\"",
                    "u. 5 x 7",
                    "v. 6 yards",
                    "w. This product is 68 cm by 22 cm by 73 cm",
                    "x. This product is 68 cm x 22 cm x 73 cm"
            };

    public static final String
            MEASURE = "(f(ee)?(oo)?t|in(ch)?(es)?|y(ar)?d(s)?|cm|m|meter(s)?|\"|\')",
            MAYBE_MEASURE = MEASURE + "?",
            NUMBER = "([0-9\\-/]+)",
            X = "(x|by)",
            SPACE = "\\s",
            MAYBE_SPACE = "\\s?",
            SPACE_OR_END = "(\\s|$)",
            START = "^",
            END = "$",
            TAB = "\t";

    public static final Pattern
            regular = Pattern.compile(NUMBER + MAYBE_SPACE + MEASURE + SPACE_OR_END, Pattern.CASE_INSENSITIVE),
            lengthXwidth = Pattern.compile(NUMBER + MAYBE_SPACE + MAYBE_MEASURE + MAYBE_SPACE + X + MAYBE_SPACE + NUMBER + MAYBE_MEASURE, Pattern.CASE_INSENSITIVE),
            beforeX = Pattern.compile(NUMBER + MAYBE_SPACE + MAYBE_MEASURE + MAYBE_SPACE + END, Pattern.CASE_INSENSITIVE),
            afterX = Pattern.compile(START + MAYBE_SPACE + NUMBER + MAYBE_SPACE + MAYBE_MEASURE, Pattern.CASE_INSENSITIVE),
            measure = Pattern.compile(MAYBE_SPACE + MEASURE, Pattern.CASE_INSENSITIVE);


    public static void getExtracted()
    {
        for (String testString:testArray)
        {
            String[] fields = {"","",""};
            int match = 0;
            Matcher lengthXwidthMatcher = lengthXwidth.matcher(testString);
            Matcher regularMatcher = regular.matcher(testString);

            if (lengthXwidthMatcher.find())
            {
                String[] split = testString.split(X);
                for (int i = 0; i<split.length; i++)
                {
                    Matcher beforeXMatcher = beforeX.matcher(split[i]);
                    Matcher afterXMatcher = afterX.matcher(split[i]);
                    if (beforeXMatcher.find() && match==0)
                    {
                        fields[0] = beforeXMatcher.group();
                        match++;
                    }
                    if (afterXMatcher.find())
                    {
                        if (match==1)
                        {
                            fields[1] = afterXMatcher.group();
                            match++;
                        }
                        else if (match==2)
                        {
                            fields[2] = afterXMatcher.group();
                            match++;
                        }
                    }
                }
                Matcher lengthHasMeasure = measure.matcher(fields[0]);
                Matcher widthHasMeasure = measure.matcher(fields[1]);
                Matcher heightHasMeasure = measure.matcher(fields[2]);
                if (heightHasMeasure.find()==true)
                {
                    if (lengthHasMeasure.find()==false)
                    {
                        fields[0] = fields[0] + heightHasMeasure.group();
                    }
                    if (widthHasMeasure.find()==false)
                    {
                        fields[1] = fields[1] + heightHasMeasure.group();
                    }
                }
                else if (widthHasMeasure.find()==true)
                {
                    if (lengthHasMeasure.find()==false)
                    {
                        fields[0] = fields[0] + widthHasMeasure.group();
                    }
                }
            }
            else if(regularMatcher.find())
            {
                fields[0] = regularMatcher.group();
                match++;
                while (regularMatcher.find() && match<3)
                {
                    fields[match] = regularMatcher.group();
                }
            }

            System.out.println(testString + " -- " + TAB + fields[0] + TAB + fields[1] + TAB + fields[2]);
        }
    }
}