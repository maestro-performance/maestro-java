/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.maestro.common.xunit.writer;

import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.xunit.*;
import org.maestro.common.xunit.Error;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class XunitWriter {
    final private Document dom;

    public XunitWriter() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new MaestroException(e);
        }

        dom = db.newDocument();
    }


    private void serializeProblem(final Element eleTestCase, final String elementName, final Problem problem) {
        if (problem == null) {
            return;
        }

        Element element = dom.createElement(elementName);

        element.setAttribute("message", problem.getMessage());
        element.setTextContent(problem.getContent());

        eleTestCase.appendChild(element);
    }


    private void serializeFailure(final Element eleTestCase, final Failure failure) {
        if (failure == null) {
            return;
        }

        serializeProblem(eleTestCase, "failure", failure);
    }

    private void serializeError(final Element eleTestCase, final Error error) {
        if (error == null) {
            return;
        }

        serializeProblem(eleTestCase, "error", error);
    }

    private void serializeTestCases(final Element eleTestSuite, final List<TestCase> testCaseList) {
        for (TestCase testCase : testCaseList) {
            Element eleTestCase = dom.createElement("testcase");

            eleTestCase.setAttribute("assertions", String.valueOf(testCase.getAssertions()));
            eleTestCase.setAttribute("classname", testCase.getClassName());
            eleTestCase.setAttribute("name", testCase.getName());
            eleTestCase.setAttribute("time", String.valueOf(testCase.getTime().getSeconds()));

            serializeFailure(eleTestCase, testCase.getFailure());
            serializeError(eleTestCase, testCase.getError());

            eleTestSuite.appendChild(eleTestCase);
        }
    }

    private void serializeTestSuite(final Element rootEle, final TestSuite testSuite) {
        Element eleTestSuite = dom.createElement("testsuite");

        eleTestSuite.setAttribute("id", testSuite.getId());
        eleTestSuite.setAttribute("name", testSuite.getName());
        eleTestSuite.setAttribute("tests", String.valueOf(testSuite.getTests()));

        serializeTestCases(eleTestSuite, testSuite.getTestCaseList());

        rootEle.appendChild(eleTestSuite);
    }

    private void serializeTestSuites(final Element rootEle, final List<TestSuite> testSuiteList) {
        for (TestSuite testSuite : testSuiteList) {
            serializeTestSuite(rootEle, testSuite);
        }
    }

    private void serializeProperty(final Element parent, Property property) {
        Element eleProperty = dom.createElement("property");

        eleProperty.setAttribute("name", property.getName());
        eleProperty.setAttribute("value", property.getValue());

        parent.appendChild(eleProperty);
    }

    private void serializePropertiesList(final Element rootEle, final List<Property> propertyList) {
        if (propertyList.size() > 0) {
            Element eleProperties = dom.createElement("properties");

            rootEle.appendChild(eleProperties);

            for (Property property : propertyList) {
                serializeProperty(eleProperties, property);
            }
        }
    }

    private void serializeProperties(final Element rootEle, final Properties properties) {
        if (properties != null) {
            serializePropertiesList(rootEle, properties.getPropertyList());
        }
    }

    public void saveToXML(final File outputFile, TestSuites testSuites) {
        Element rootEle = dom.createElement("testsuites");
        serializeTestSuites(rootEle, testSuites.getTestSuiteList());
        serializeProperties(rootEle, testSuites.getProperties());
        dom.appendChild(rootEle);

        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();

            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(outputFile)));

        } catch (IOException | TransformerException te) {
            throw new MaestroException(te);
        }
    }
}
