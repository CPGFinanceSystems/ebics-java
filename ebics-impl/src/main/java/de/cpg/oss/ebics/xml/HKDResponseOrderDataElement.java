package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.BankAccountInformation;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h004.AccountType;
import org.ebics.h004.HKDResponseOrderDataType;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HKDResponseOrderDataElement implements ResponseOrderDataElement<HKDResponseOrderDataType> {

    @Getter
    private final HKDResponseOrderDataType responseOrderData;

    public static HKDResponseOrderDataElement parse(final InputStream orderDataXml) throws EbicsException {
        return new HKDResponseOrderDataElement(XmlUtil.parse(HKDResponseOrderDataType.class, orderDataXml));
    }

    @Override
    public Class<HKDResponseOrderDataType> getResponseOrderDataClass() {
        return HKDResponseOrderDataType.class;
    }

    public Collection<BankAccountInformation> getBankAccounts() {
        return Optional.ofNullable(responseOrderData.getPartnerInfo().getAccountInfos())
                .map(infos -> infos.stream()
                        .map(info -> BankAccountInformation.builder()
                                .id(info.getID())
                                .accountHolder(info.getAccountHolder())
                                .currency(info.getCurrency())
                                .description(info.getDescription())
                                .accountNmber(accountNumber(info.getAccountNumbersAndNationalAccountNumbers()))
                                .bankCode(bankCode(info.getBankCodesAndNationalBankCodes()))
                                .iban(iban(info.getAccountNumbersAndNationalAccountNumbers()))
                                .bic(bic(info.getBankCodesAndNationalBankCodes()))
                                .build()))
                .orElse(Stream.empty())
                .collect(Collectors.toList());
    }

    private static String accountNumber(final List<?> accountNumbers) {
        return accountNumbers.stream()
                .filter(AccountType.NationalAccountNumber.class::isInstance)
                .findFirst()
                .map(accountNumber -> ((AccountType.NationalAccountNumber) accountNumber).getValue())
                .orElse(null);
    }

    private static String bankCode(final List<?> bankCodes) {
        return bankCodes.stream()
                .filter(AccountType.NationalBankCode.class::isInstance)
                .findFirst()
                .map(bankCode -> ((AccountType.NationalBankCode) bankCode).getValue())
                .orElse(null);
    }

    private static String iban(final List<?> accountNumbers) {
        return accountNumbers.stream()
                .filter(AccountType.AccountNumber.class::isInstance)
                .map(AccountType.AccountNumber.class::cast)
                .filter(AccountType.AccountNumber::isInternational)
                .findFirst()
                .map(AccountType.AccountNumber::getValue)
                .orElse(null);
    }

    private static String bic(final List<?> bankCodes) {
        return bankCodes.stream()
                .filter(AccountType.BankCode.class::isInstance)
                .map(AccountType.BankCode.class::cast)
                .filter(AccountType.BankCode::isInternational)
                .findFirst()
                .map(AccountType.BankCode::getValue)
                .orElse(null);
    }

    private static boolean isListOf(final List<?> list, final Class<?> clazz) {
        return list.stream().findFirst().map(clazz::isInstance).orElse(false);
    }
}
