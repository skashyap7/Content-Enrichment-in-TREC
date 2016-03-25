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

import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.CleanPhoneText;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MeasurementExtractingContentHandler extends ContentHandlerDecorator {
    private Metadata metadata;
    private static final String MEASUREMENTS = "measurements";

    /**
     * Creates a decorator for the given SAX event handler and Metadata object.
     *
     * @param handler SAX event handler to be decorated
     */
    public MeasurementExtractingContentHandler(ContentHandler handler, Metadata metadata) {
        super(handler);
        this.metadata = metadata;
    }

    /**
     * Creates a decorator that by default forwards incoming SAX events to
     * a dummy content handler that simply ignores all the events. Subclasses
     * should use the {@link #setContentHandler(ContentHandler)} method to
     * switch to a more usable underlying content handler.
     * Also creates a dummy Metadata object to store phone numbers in.
     */
    protected MeasurementExtractingContentHandler() {
        this(new DefaultHandler(), new Metadata());
    }


    /**
     * This method is called whenever the Parser is done parsing the file. So,
     */
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        //List<String> numbers = CleanPhoneText.extractPhoneNumbers(super.toString());
        List<String> numbers = extractMeasurements(super.toString());
        for (String number : numbers) {
            metadata.add(MEASUREMENTS, number);
        }
    }

    public static final String
            MEASURE = "(f(ee)?(oo)?t|in(ch)?(es)?|y(ar)?d(s)?|kg|g|ml|l|km|cm|m|meter(s)?|\"|\')",
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


    public static ArrayList<String> extractMeasurements(String text)
    {
        ArrayList<String> measurements = new ArrayList<String>();
        //for (String text:inputText.split("\n"))
       // {
            String[] fields = {"","",""};
            int match = 0;
            Matcher lengthXwidthMatcher = lengthXwidth.matcher(text);
            Matcher regularMatcher = regular.matcher(text);

            if (lengthXwidthMatcher.find())
            {
                String[] split = text.split(X);
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

            measurements.add(" -- " + TAB + fields[0] + TAB + fields[1] + TAB + fields[2]);
//}
        return measurements;
    }

}
