package de.cpg.oss.ebics.xml;

public interface ResponseOrderDataElement<T> {

    Class<T> getResponseOrderDataClass();

    T getResponseOrderData();
}
