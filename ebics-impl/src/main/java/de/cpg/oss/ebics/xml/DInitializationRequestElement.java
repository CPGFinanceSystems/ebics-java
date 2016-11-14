/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import org.ebics.h004.*;

import java.time.LocalDate;


/**
 * The <code>DInitializationRequestElement</code> is the common initialization
 * for all ebics downloads.
 *
 * @author Hachani
 */
public class DInitializationRequestElement extends InitializationRequestElement {

    private final LocalDate startRange;
    private final LocalDate endRange;

    /**
     * Constructs a new <code>DInitializationRequestElement</code> for downloads initializations.
     *
     * @param session    the current ebics session
     * @param type       the download order type (FDL, HTD, HPD)
     * @param startRange the start range download
     * @param endRange   the end range download
     */
    public DInitializationRequestElement(final EbicsSession session,
                                         final OrderType type,
                                         final LocalDate startRange,
                                         final LocalDate endRange) {
        super(session, type);
        this.startRange = startRange;
        this.endRange = endRange;
    }

    @Override
    public EbicsRequest buildInitialization() throws EbicsException {
        final StaticHeaderOrderDetailsType orderDetails = OBJECT_FACTORY.createStaticHeaderOrderDetailsType();
        orderDetails.setOrderAttribute(OrderAttributeType.DZHNN);
        orderDetails.setOrderType(EbicsXmlFactory.orderType(type));
        if (type.equals(OrderType.FDL)) {
            final FileFormatType fileFormat = OBJECT_FACTORY.createFileFormatType();
            fileFormat.setCountryCode(session.getConfiguration().getLocale().getCountry().toUpperCase());
            fileFormat.setValue(session.getSessionParam("FORMAT"));

            final FDLOrderParamsType fDLOrderParamsType = OBJECT_FACTORY.createFDLOrderParamsType();
            fDLOrderParamsType.setFileFormat(fileFormat);

            if (startRange != null && endRange != null) {
                final FDLOrderParamsType.DateRange range = OBJECT_FACTORY.createFDLOrderParamsTypeDateRange();
                range.setStart(startRange);
                range.setEnd(endRange);

                fDLOrderParamsType.setDateRange(range);
            }

            if (Boolean.getBoolean(session.getSessionParam("TEST"))) {
                final Parameter.Value value = OBJECT_FACTORY.createParameterValue();
                value.setType("String");
                value.setValue("TRUE");

                final Parameter parameter = OBJECT_FACTORY.createParameter();
                parameter.setName("TEST");
                parameter.setValue(value);

                fDLOrderParamsType.getParameters().add(parameter);
            }

            orderDetails.setOrderParams(OBJECT_FACTORY.createFDLOrderParams(fDLOrderParamsType));
        } else {
            final StandardOrderParamsType standardOrderParamsType = OBJECT_FACTORY.createStandardOrderParamsType();

            //FIXME Some banks cannot handle OrderID element in download process. Add parameter in configuration!!!
            orderDetails.setOrderID(null);
            orderDetails.setOrderParams(OBJECT_FACTORY.createStandardOrderParams(standardOrderParamsType));
        }

        return EbicsXmlFactory.request(
                session.getConfiguration(),
                EbicsXmlFactory.header(
                        EbicsXmlFactory.mutableHeader(TransactionPhaseType.INITIALISATION),
                        EbicsXmlFactory.staticHeader(session, nonce, orderDetails)));
    }
}
