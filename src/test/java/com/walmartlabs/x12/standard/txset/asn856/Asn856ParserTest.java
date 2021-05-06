/**
Copyright (c) 2018-present, Walmart, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.walmartlabs.x12.standard.txset.asn856;

import com.walmartlabs.x12.X12Document;
import com.walmartlabs.x12.X12TransactionSet;
import com.walmartlabs.x12.exceptions.X12ErrorDetail;
import com.walmartlabs.x12.standard.InterchangeControlEnvelope;
import com.walmartlabs.x12.standard.StandardX12Document;
import com.walmartlabs.x12.standard.StandardX12Parser;
import com.walmartlabs.x12.standard.X12Group;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Asn856ParserTest {

    private StandardX12Parser asnParser;

    @Before
    public void init() {
        asnParser = new StandardX12Parser();
        asnParser.registerTransactionSetParser(new DefaultAsn856TransactionSetParser());
    }

    @Test
    public void test_Parsing_SourceIsNull() throws IOException {
        String sourceData = null;
        X12Document x12 = asnParser.parse(sourceData);
        assertNull(x12);
    }

    @Test
    public void test_Parsing_SourceIsEmpty() throws IOException {
        String sourceData = "";
        X12Document x12 = asnParser.parse(sourceData);
        assertNull(x12);
    }

    @Test
    public void test_Parsing_Asn856() throws IOException {
        byte[] asnBytes = Files.readAllBytes(Paths.get("src/test/resources/asn856/asn856.txt"));
        StandardX12Document x12 = asnParser.parse(new String(asnBytes));
        assertNotNull(x12);

        // ISA segment
        InterchangeControlEnvelope isa = x12.getInterchangeControlEnvelope();
        assertNotNull(isa);
        assertEquals("01", isa.getAuthorizationInformationQualifier());
        assertEquals("0000000000", isa.getAuthorizationInformation());
        assertEquals("01", isa.getSecurityInformationQualifier());
        assertEquals("0000000000", isa.getSecurityInformation());
        assertEquals("ZZ", isa.getInterchangeIdQualifier());
        assertEquals("ABCDEFGHIJKLMNO", isa.getInterchangeSenderId());
        assertEquals("ZZ", isa.getInterchangeIdQualifierTwo());
        assertEquals("123456789012345", isa.getInterchangeReceiverId());
        assertEquals("101127", isa.getInterchangeDate());
        assertEquals("1719", isa.getInterchangeTime());
        assertEquals("U", isa.getInterchangeControlStandardId());
        assertEquals("00400", isa.getInterchangeControlVersion());
        assertEquals("000003438", isa.getInterchangeControlNumber());
        assertEquals("0", isa.getAcknowledgementRequested());
        assertEquals("P", isa.getUsageIndicator());
        assertEquals(">", isa.getElementSeparator());
        
        // Groups
        assertEquals(new Integer(1), isa.getNumberOfGroups());
        assertEquals("000000049", isa.getTrailerInterchangeControlNumber());

        List<X12Group> groups = x12.getGroups();
        assertNotNull(groups);
        assertEquals(1, groups.size());

        // Transaction Sets
        List<X12TransactionSet> txForGroupOne = x12.getGroups().get(0).getTransactions();
        assertNotNull(txForGroupOne);
        assertEquals(1, txForGroupOne.size());

        // ST
        AsnTransactionSet asnTx = (AsnTransactionSet) txForGroupOne.get(0);
        assertEquals("856", asnTx.getTransactionSetIdentifierCode());
        assertEquals("0008", asnTx.getHeaderControlNumber());
        
        assertTrue(asnTx.isLoopingValid());
        List<X12ErrorDetail> loopErrors = asnTx.getLoopingErrors();
        assertNull(loopErrors);
        
        // BSN
        assertEquals("14", asnTx.getPurposeCode());
        assertEquals("829716", asnTx.getShipmentIdentification());
        assertEquals("20111206", asnTx.getShipmentDate());
        assertEquals("142428", asnTx.getShipmentTime());
        assertEquals("0002", asnTx.getHierarchicalStructureCode());
        
        // SE
        assertEquals(Integer.valueOf(31), asnTx.getExpectedNumberOfSegments());
        assertEquals("0008", asnTx.getTrailerControlNumber());
    }
    

    @Test
    public void test_Parsing_Asn856_badLoops() throws IOException {
        byte[] asnBytes = Files.readAllBytes(Paths.get("src/test/resources/asn856/asn856.txt"));
        String sourceData = new String(asnBytes);
        sourceData = sourceData.replace("HL*2*1*O", "HL*2*99*O");
        
        StandardX12Document x12 = asnParser.parse(sourceData);
        assertNotNull(x12);

        // ISA segment
        InterchangeControlEnvelope isa = x12.getInterchangeControlEnvelope();
        assertNotNull(isa);
        assertEquals("01", isa.getAuthorizationInformationQualifier());
        assertEquals("0000000000", isa.getAuthorizationInformation());
        assertEquals("01", isa.getSecurityInformationQualifier());
        assertEquals("0000000000", isa.getSecurityInformation());
        assertEquals("ZZ", isa.getInterchangeIdQualifier());
        assertEquals("ABCDEFGHIJKLMNO", isa.getInterchangeSenderId());
        assertEquals("ZZ", isa.getInterchangeIdQualifierTwo());
        assertEquals("123456789012345", isa.getInterchangeReceiverId());
        assertEquals("101127", isa.getInterchangeDate());
        assertEquals("1719", isa.getInterchangeTime());
        assertEquals("U", isa.getInterchangeControlStandardId());
        assertEquals("00400", isa.getInterchangeControlVersion());
        assertEquals("000003438", isa.getInterchangeControlNumber());
        assertEquals("0", isa.getAcknowledgementRequested());
        assertEquals("P", isa.getUsageIndicator());
        assertEquals(">", isa.getElementSeparator());
        
        // Groups
        assertEquals(new Integer(1), isa.getNumberOfGroups());
        assertEquals("000000049", isa.getTrailerInterchangeControlNumber());

        List<X12Group> groups = x12.getGroups();
        assertNotNull(groups);
        assertEquals(1, groups.size());

        // Transaction Sets
        List<X12TransactionSet> txForGroupOne = x12.getGroups().get(0).getTransactions();
        assertNotNull(txForGroupOne);
        assertEquals(1, txForGroupOne.size());

        // ST
        AsnTransactionSet asnTx = (AsnTransactionSet) txForGroupOne.get(0);
        assertEquals("856", asnTx.getTransactionSetIdentifierCode());
        assertEquals("0008", asnTx.getHeaderControlNumber());
        
        assertFalse(asnTx.isLoopingValid());
        List<X12ErrorDetail> loopErrors = asnTx.getLoopingErrors();
        assertNotNull(loopErrors);
        assertEquals(1, loopErrors.size());
        assertEquals("HL segment (2) is missing parent (99)", loopErrors.get(0).getMessage());
        
        // BSN
        assertEquals("14", asnTx.getPurposeCode());
        assertEquals("829716", asnTx.getShipmentIdentification());
        assertEquals("20111206", asnTx.getShipmentDate());
        assertEquals("142428", asnTx.getShipmentTime());
        assertEquals("0002", asnTx.getHierarchicalStructureCode());
        
        // SE
        assertEquals(Integer.valueOf(31), asnTx.getExpectedNumberOfSegments());
        assertEquals("0008", asnTx.getTrailerControlNumber());
    }
    
}
