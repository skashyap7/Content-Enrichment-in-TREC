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
import org.apache.tika.sax.ContentHandlerDecorator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;
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
        List<Measurement> numbers = extractMeasurements(super.toString());
        for (Measurement number : numbers) {
            metadata.add(MEASUREMENTS, number.toString());
        }
    }

    public static ArrayList<Measurement> extractMeasurements(String text)
    {
        ArrayList<Measurement> measurements = new ArrayList<>();
        Matcher regularMatcher = regular.matcher(text);
        Set<String> namedGroups = getNamedGroupCandidates(NUMBER + MAYBE_SPACE + MEASURE + SPACE_OR_END);

        if (regularMatcher.find()) {
            // Remove invalid groups
            Iterator<String> i = namedGroups.iterator();
            while (i.hasNext()) {
                try {
                    regularMatcher.group(i.next());
                } catch (IllegalArgumentException e) {
                    i.remove();
                }
            }
            measurements.add(printMatches(regularMatcher, namedGroups));
            while (regularMatcher.find()) {
                measurements.add(printMatches(regularMatcher, namedGroups));
            }
        }
        return measurements;
    }

    private static Measurement printMatches(Matcher matcher, Set<String> namedGroups) {
        Measurement m=new Measurement();
        String[] measure= matcher.group().split(" ",2);
        m.set_unit_value(measure[0]);
        m.set_unit(measure[1]);
        for (String name: namedGroups) {
            String matchedString = matcher.group(name);
            if (matchedString != null) {
                measure=name.split("0");
                m.set_category(measure[0]);
                if(measure.length<2)
                    m.set_sub_category(name);
                else
                    m.set_sub_category(measure[1]);
            }
        }
        return m;
    }

    private static Set<String> getNamedGroupCandidates(String regex) {
        Set<String> namedGroups = new TreeSet<String>();
        Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(regex);
        while (m.find()) {
            namedGroups.add(m.group(1));
        }
        return namedGroups;
    }

    public static final String
            MEASURE = "(?<unit>(?:(?:atto|centi|deca|deci|exa|femto|giga|hecto|kilo|mega|micro|milli|nano|peta|pico|tera|yocto|yotta|zepto)" +
            "?\\s*(?:(?:(?:(?<distance0metre>met)|(?<volume0litre>lit))(?:re|er))|(?<weight0gram>gram)|(?<energy>calorie|joule)(?<time0second>sec(?:ond)?))s?)|" +
            "(?:(?<distance0feet>f(?:ee)?(?:oo)?t)|(?<distance0inch>in(?:ch)?(?:es)?)|(?<distance0yard>y(?:ar)?ds?)|(?<distance0mile>miles?)|" +
            "(?<distance0inch01>\\\")|(?<distance0feet01>\\'))|(?<temperature>celsius|fahrenheit|kelvin)|(?<time0other>h(?:ou)r|day|min(?:ute)?|y(?:ea)r)s?|" +
            "(?:(?<volume>gallon|pint|quart|cup)s?|(?<volume0spoon>tsp|tbsp|(?:table|tea)\\s*spoons?)|(?<volume0quart>qt)|(?<volume0cubic>cubic\\s*(?:(?:centi)?met(?:er|re)s?|" +
            "f(?:ee)?(?:oo)?t|in(?:ch)?(?:es)?)))|(?:(?<weight>ounce|pound)s?|(?<weight0ounce>oz)|(?<weight0pound>lb)|(?<weight0ton>(?:metric|long|short)?\\s*ton(?:ne)s?))|" +
            "(?:[acdefghkmtyz]?(?:(?<energy0calorie01>c)|(?<energy0joule01>j)|(?<distance0metre01>m)|(?<volume0litre01>l)|(?<weight0gram01>g)|(?<time0second01>sec))))",
            /*
            // the below is the expanded form of above regex for matching measurement units
            // to convert the below to actual regex string you need to do 2 things
            // 1. remove all the lines that start with '//' i.e regex version => '//.*'
            // 2. replace '[\n\t\s]*' with empty string to get the final regex
            // or simple way replace '//.*|[\t\n\s]' in below string with empty string
            // use https://myregextester.com/index.php to dig deep

            // for all the measurement units
            (?<unit>
                     (?:(?<distance0metre>met)|(?<volume0litre>lit))
                     (?:re|er)

                 )|
                 (?<weight0gram>gram)|
                 (?<energy>
                     calorie|
                     joule
                 )
                 (?<time0second>sec(?:ond)?)
             )s?
            )|


            // other distance units like feet, inches, yards etc..
            (?:
             (?<distance0feet>f(?:ee)?(?:oo)?t)|
             (?<distance0inch>in(?:ch)?(?:es)?)|
             (?<distance0yard>y(?:ar)?ds?)|
             (?<distance0mile>miles?)|
             (?<distance0inch01>\")|
             (?<distance0feet01>\')
            )|


            // temperature units
            (?<temperature>
             celsius|
             fahrenheit|
             kelvin
            )|


            // time units
            (?<time0other>
             h(?:ou)r|
             day|
             min(?:ute)?|
             y(?:ea)r
            )s?|


            // volume units
            (?:
             (?<volume>
                 gallon|
                 pint|
                 quart|
                 cup
             )s?|

             (?<volume0spoon>
                 tsp|
                 tbsp|
                 (?:table|tea)\s*spoons?
             )|

             (?<volume0quart>qt)|

             // cubic versions
             (?<volume0cubic>
                 cubic
                 \s*
                 (?:
                     (?:centi)?met(?:er|re)s?|
                     f(?:ee)?(?:oo)?t|
                     in(?:ch)?(?:es)?
                 )
             )
            )|

            // weight units
            (?:
             (?<weight>
                 ounce|
                 pound
             )s?|

             (?<weight0ounce>oz)|
             (?<weight0pound>lb)|

             // ton postfix versions
             (?<weight0ton>
                 (?:metric|long|short)?
                 \s*
                 ton(?:ne)s?
             )
            )|

            // short form abrevations of all the above full units
            // these should come at last so that dont match the single chars before the whole word matches
            (?:
             [acdefghkmtyz]?
             (?:
                 // c => calorie , j=> joule, m => metre, l=> litre, g=> gram
                 (?<energy0calorie01>c)|
                 (?<energy0joule01>j)|
                 (?<distance0metre01>m)|
                 (?<volume0litre01>l)|
                 (?<weight0gram01>g)|
                 (?<time0second01>sec)
             )
            )
            )
        */
            NUMBER = "([\\d\\-\\./]+)",
            MAYBE_SPACE = "\\s?",
            SPACE_OR_END = "(\\W|$)";

    public static final Pattern
            regular = Pattern.compile(NUMBER + MAYBE_SPACE + MEASURE + SPACE_OR_END, Pattern.CASE_INSENSITIVE);


}
