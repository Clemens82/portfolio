package name.abuchen.portfolio.datatransfer.pdf;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.abuchen.portfolio.datatransfer.pdf.PDFParser.Block;
import name.abuchen.portfolio.datatransfer.pdf.PDFParser.DocumentType;
import name.abuchen.portfolio.datatransfer.pdf.PDFParser.Transaction;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.BuySellEntry;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.model.Transaction.Unit;
import name.abuchen.portfolio.money.Money;
import name.abuchen.portfolio.money.Values;

@SuppressWarnings("nls")
public class RocheConnectPDFExtractor extends AbstractPDFExtractor
{
    private static final String FLAG_WITHHOLDING_TAX_FOUND = "exchangeRate"; //$NON-NLS-1$
    
     private static final DateTimeFormatter DATE_FORMAT_EQUATEX = DateTimeFormatter.ofPattern("dd. MMM yyyy", Locale.GERMANY); //$NON-NLS-1$
     private static final DateTimeFormatter DATE_FORMAT_EQUATEX2 = DateTimeFormatter.ofPattern("dd. MMM yyyy", Locale.US); //$NON-NLS-1$
     private static DateTimeFormatter DATE_FORMAT_UBS = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern( "dd.MMMyyyy" ).toFormatter( Locale.GERMANY );
     private static final DateTimeFormatter DATE_FORMAT_UBS2 = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern( "dd.MMMyyyy" ).toFormatter( Locale.US );
    
    private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);


    public RocheConnectPDFExtractor(Client client)
    {
        super(client);

        addBankIdentifier("Roche Connect"); //$NON-NLS-1$
        addBankIdentifier("Equatex AG");
        //addBankIdentifier("UBS AG");

        addBuySellTransaction();
        addDividendeTransaction();
    }

    @Override
    public String getLabel()
    {
        return "RocheConnect"; //$NON-NLS-1$
    }

    private void addBuySellTransaction()
    {
        //DocumentType type = new DocumentType("Roche Connect - (Neue Käufe)");
        DocumentType type = new DocumentType("Roche Connect.*");
        type.setTrimFile("Roche Connect.*- Verfügbar"); // Roche Connect (Genussscheine Purchase Plan)? - Verfügbar

        this.addDocumentTyp(type);

        Transaction<BuySellEntry> pdfTransaction = new Transaction<>();
        pdfTransaction.subject(() -> {
            BuySellEntry entry = new BuySellEntry();
            entry.setType(PortfolioTransaction.Type.BUY);
            return entry;
        });

        //Block firstRelevantLine = new Block("Roche Connect - (Neue Käufe)");
        String tableRegEx = "^(?<date>[\\d]+.\\s*[\\wä]+\\.?\\s*[\\d]+) Roche Genussscheine\\s*(CHF)?\\s*(?<buyin>[\\d.]+)\\s*(CHF)?\\s*.*\\d{4} (?<shares>[\\d\\.]+)$";
        Block firstRelevantLine = new Block(tableRegEx);
        
        type.addBlock(firstRelevantLine);
        firstRelevantLine.set(pdfTransaction);
        
        // ISIN CH0012032048 WKN 855167 SYMBOL Symbol: ROG
        Map<String, String> rocheAktie = new TreeMap<String, String>();
        rocheAktie.put("isin", "CH0012032048");
        rocheAktie.put("wkn", "855167");
        rocheAktie.put("name", "Roche Genussscheine");
        rocheAktie.put("currency", "CHF");
        
        // has Sept. and Juni but need Jun. Sep.
        DateFormatSymbols dfsFr = new DateFormatSymbols(Locale.GERMAN);
        String[] oldMonths = dfsFr.getShortMonths();
        String[] newMonths = new String[oldMonths.length];
        for (int i = 0, len = oldMonths.length; i < len; ++ i) {
            String oldMonth = oldMonths[i];
            //System.out.println(oldMonth);
            newMonths[i] = oldMonth.substring(0, Math.min(3, oldMonth.length())) + ".";
            //System.out.println(newMonths[i]);
        }
        dfsFr.setShortMonths(newMonths);
        SimpleDateFormat LOCAL_DATE = new SimpleDateFormat("dd.MMMyyyy", dfsFr);

        pdfTransaction

                        // Is type --> "Verkauf" change from BUY to SELL
                        .section("type").optional().match("Roche Connect (?<type>(Verkauf|R.cknahme Investmentfonds)).*").assign((t, v) -> {
                            if (v.get("type").equals("Verkauf") || v.get("type").equals("Rücknahme Investmentfonds"))
                            {
                                t.setType(PortfolioTransaction.Type.SELL);
                            }
                        })


                        // Zuteilungsdatum Instrument Kosten-basis Beschränkungs-datum Im Zeitraum gekaufte Menge
                        // 19. Jul 2021 Roche Genussscheine 357.52 CHF 19. Jul 2024 1.58631
                        .section("date", "buyin", "shares")
                        .match(tableRegEx)                        
                        .assign((t, v) -> {
                            System.out.println(v.get("buyin"));
                            System.out.println(asShares(v.get("shares")));
                            //long amount = asAmount(v.get("buyin")) * asShares(v.get("shares"));
                            long amount = Math.round(Double.parseDouble(v.get("buyin")) * Double.parseDouble(v.get("shares")) * Values.Amount.factor());
                            t.setAmount(amount);
                            t.setSecurity(getOrCreateSecurity(rocheAktie));
                            t.setShares(asShares(v.get("shares")));
                            t.setCurrencyCode(rocheAktie.get("currency"));
                            //LocalDateTime ldt = LocalDateTime.parse("2014-07-18T00:00:00");
                            //System.out.println(ldt.format(DATE_FORMAT_UBS2));//18.Jul.2014
                            t.setDate(superDateParser(v.get("date")));
                            /*
                            try
                            {
                                System.out.println(LOCAL_DATE.format( new SimpleDateFormat("yyyy-MM-dd").parse("2015-05-18") ));
                            }
                            catch (ParseException e5)
                            {
                                // TODO Auto-generated catch block
                                e5.printStackTrace();
                            }
                            v.put("date", v.get("date").replace("Jul.2", "Jul2").replace("Sep.2", "Sep2"));
                            try {
                                t.setDate(LocalDate.parse(v.get("date"), DATE_FORMAT_EQUATEX).atStartOfDay());
                            } catch (DateTimeParseException e) {
                                try {
                                    t.setDate(LocalDate.parse(v.get("date"), DATE_FORMAT_EQUATEX2).atStartOfDay());
                                } catch (DateTimeParseException e2) {
                                    try {
                                        t.setDate(LocalDate.parse(v.get("date"), DATE_FORMAT_UBS).atStartOfDay());
                                    } catch (DateTimeParseException e3) {
                                        try {

                                            t.setDate(LocalDate.parse(v.get("date"), DATE_FORMAT_UBS2).atStartOfDay());
                                        } catch (DateTimeParseException e4) {
                                            try {
                                                Date in = LOCAL_DATE.parse(v.get("date"));
                                                LocalDateTime di = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
                                                t.setDate(di);
                                            } catch (ParseException e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }*/
                        })

                        .wrap(BuySellEntryItem::new);

        //addTaxesSectionsTransaction(pdfTransaction, type);
        //addFeesSectionsTransaction(pdfTransaction, type);
    }
    
    public LocalDateTime superDateParser(String datee) {
        Matcher m = Pattern.compile("^(\\d+)[\\W]*([\\d\\wäöü]+)[\\W]*(\\d+)\\s*$").matcher(datee);
        m.find();
        
        System.out.println(datee);
        for (int i=0; i<=m.groupCount(); i++)
          System.out.println(i + " => " + m.group(i));
        
        
        // has Sept. and Juni but need Jun. Sep.
        DateFormatSymbols dfsFr = new DateFormatSymbols(Locale.GERMAN);
        String[] oldMonths = dfsFr.getShortMonths();
        
        String month = m.group(2);
        if (Pattern.matches("^[\\wä]+$", month)) {
            month = month.toLowerCase();
            for (int i=1;i<=12;i++) {
                if (month.startsWith(oldMonths[i-1].toLowerCase().substring(0,3))) {
                    month = Integer.toString(i);
                    break;
                }
            }
            if (month.startsWith("mar") || month.startsWith("mär")) {
                month = "3";
            } else if(month.startsWith("jän")) {
                month = "1";
            } else if(month.startsWith("may")) {
                month = "5";
            } else if(month.startsWith("oct")) {
                month = "10";
            } else if(month.startsWith("dec")) {
                month = "12";
            }
        }
        
        String isoDate = m.group(1) + "-" + month + "-" + m.group(3);
        
        return asDate(isoDate);
    }
    
    protected long asShares(String value)
    {
        try
        {
            return Math.round(numberFormat.parse(value).doubleValue() * Values.Share.factor());
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
    
    protected long asAmount(String value)
    {
        try
        {
            return Math.abs(Math.round(numberFormat.parse(value).doubleValue() * Values.Amount.factor()));
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    private String fixDateTime(String dateTimeString, char divider)
    {
        dateTimeString = dateTimeString.replaceAll("\\s", "");
        StringBuffer s = new StringBuffer(dateTimeString);
        if (dateTimeString.charAt(2)!=divider) {
            s.insert(2, divider);
        }
        if (s.toString().charAt(5)!=divider) {
            if (!Character.isDigit(s.toString().charAt(5))) {
                s.deleteCharAt(5);
            }
            s.insert(5, divider);
        }
        return s.toString();
    }

    private void addDividendeTransaction()
    {
        DocumentType type = new DocumentType("(Dividendengutschrift|Aussch.ttung Investmentfonds|Gutschrift von Investmentertr.gen|Ertragsgutschrift)");
        this.addDocumentTyp(type);

        Block block = new Block("(Dividendengutschrift|Aussch.ttung Investmentfonds|Gutschrift von Investmentertr.gen|Ertragsgutschrift.*)");
        type.addBlock(block);
        Transaction<AccountTransaction> pdfTransaction = new Transaction<AccountTransaction>().subject(() -> {
            AccountTransaction entry = new AccountTransaction();
            entry.setType(AccountTransaction.Type.DIVIDENDS);
            return entry;
        });

        pdfTransaction

                        // Stück 12 JOHNSON & JOHNSON SHARES US4781601046
                        // (853260)
                        // REGISTERED SHARES DL 1
                        .section("isin", "wkn", "name", "shares", "name1")
                        .match("^St.ck (?<shares>[\\d.,]+) (?<name>.*) (?<isin>[\\w]{12}.*) (\\((?<wkn>.*)\\).*)")
                        .match("(?<name1>.*)") //
                        .assign((t, v) -> {
                            if (!v.get("name1").startsWith("Zahlbarkeitstag"))
                                v.put("name", v.get("name").trim() + " " + v.get("name1"));

                            t.setSecurity(getOrCreateSecurity(v));
                            t.setShares(asShares(v.get("shares")));
                        })

                        // Ex-Tag 22.02.2021 Art der Dividende Quartalsdividende
                        .section("date") //
                        .match("^Ex-Tag (?<date>\\d+.\\d+.\\d{4}).*") //
                        .assign((t, v) -> {
                            t.setDateTime(asDate(v.get("date")));
                        })

                        // Ex-Tag 22.02.2021 Art der Dividende Quartalsdividende
                        .section("note").optional() //
                        .match("^Ex-Tag \\d+.\\d+.\\d{4} Art der Dividende (?<note>.*)").assign((t, v) -> {
                            t.setNote(v.get("note"));
                        })

                        // Ausmachender Betrag 8,64+ EUR
                        .section("currency", "amount")
                        .match("^Ausmachender Betrag (?<amount>[\\d.]+,\\d+)\\+ (?<currency>\\w{3})") //
                        .assign((t, v) -> {
                            t.setAmount(asAmount(v.get("amount")));
                            t.setCurrencyCode(v.get("currency"));
                        })

                        // Devisenkurs EUR / USD 1,1920
                        // Devisenkursdatum 09.03.2021
                        // Dividendengutschrift 12,12 USD 10,17+ EUR
                        .section("exchangeRate", "fxAmount", "fxCurrency", "amount", "currency").optional()
                        .match("^Devisenkurs .* (?<exchangeRate>[\\d.]+,\\d+)$") //
                        .match("^Devisenkursdatum .*") //
                        .match("^(Dividendengutschrift|Aussch.ttung) (?<fxAmount>[\\d.]+,\\d+) (?<fxCurrency>\\w{3}) (?<amount>[\\d.]+,\\d+)\\+ (?<currency>\\w{3})$")
                        .assign((t, v) -> {
                            BigDecimal exchangeRate = asExchangeRate(v.get("exchangeRate"));
                            if (t.getCurrencyCode().contentEquals(asCurrencyCode(v.get("fxCurrency"))))
                            {
                                exchangeRate = BigDecimal.ONE.divide(exchangeRate, 10, RoundingMode.HALF_DOWN);
                            }
                            type.getCurrentContext().put("exchangeRate", exchangeRate.toPlainString());

                            if (!t.getCurrencyCode().equals(t.getSecurity().getCurrencyCode()))
                            {
                                BigDecimal inverseRate = BigDecimal.ONE.divide(exchangeRate, 10,
                                                RoundingMode.HALF_DOWN);

                                // check, if forex currency is transaction
                                // currency or not and swap amount, if necessary
                                Unit grossValue;
                                if (!asCurrencyCode(v.get("fxCurrency")).equals(t.getCurrencyCode()))
                                {
                                    Money fxAmount = Money.of(asCurrencyCode(v.get("fxCurrency")),
                                                    asAmount(v.get("fxAmount")));
                                    Money amount = Money.of(asCurrencyCode(v.get("currency")),
                                                    asAmount(v.get("amount")));
                                    grossValue = new Unit(Unit.Type.GROSS_VALUE, amount, fxAmount, inverseRate);
                                }
                                else
                                {
                                    Money amount = Money.of(asCurrencyCode(v.get("fxCurrency")),
                                                    asAmount(v.get("fxAmount")));
                                    Money fxAmount = Money.of(asCurrencyCode(v.get("currency")),
                                                    asAmount(v.get("amount")));
                                    grossValue = new Unit(Unit.Type.GROSS_VALUE, amount, fxAmount, inverseRate);
                                }
                                t.addUnit(grossValue);
                            }
                        })

                        .wrap(TransactionItem::new);

        addTaxesSectionsTransaction(pdfTransaction, type);
        addFeesSectionsTransaction(pdfTransaction, type);

        block.set(pdfTransaction);
    }

    private <T extends Transaction<?>> void addTaxesSectionsTransaction(T transaction, DocumentType type)
    {
        transaction
                        // Einbehaltene Quellensteuer 15 % auf 12,12 USD 1,53-
                        // EUR
                        .section("quellensteinbeh", "currency").optional()
                        .match("^Einbehaltende Quellensteuer [.,\\d]+ % .* (?<quellensteinbeh>[.,\\d]+)- (?<currency>[\\w]{3})$")
                        .assign((t, v) -> {
                            type.getCurrentContext().put(FLAG_WITHHOLDING_TAX_FOUND, "true");
                            addTax(type, t, v, "quellensteinbeh");
                        })

                        // Anrechenbare Quellensteuer 15 % auf 10,17 EUR 1,53
                        // EUR
                        .section("quellenstanr", "currency").optional()
                        .match("^Anrechenbare Quellensteuer [.,\\d]+ % .* [.,\\d]+ \\w{3} (?<quellenstanr>[.,\\d]+) (?<currency>\\w{3})$")
                        .assign((t, v) -> addTax(type, t, v, "quellenstanr"))
                        
                        // Anrechenbare Quellensteuer pro Stück 0,0144878 EUR 0,29 EUR
                        .section("quellenstanr", "currency").optional()
                        .match("^Anrechenbare Quellensteuer pro St.ck [.,\\d]+ \\w{3} (?<quellenstanr>[.,\\d]+) (?<currency>\\w{3})$")
                        .assign((t, v) -> addTax(type, t, v, "quellenstanr"))

        
                        // Kapitalertragsteuer 24,51% auf 0,71 EUR 0,17- EUR
                        .section("tax", "currency").optional()
                        .match("^Kapitalertragsteuer [\\d,.]+% auf [.,\\d]+ [\\w]{3} (?<tax>[.,\\d]+)- (?<currency>[\\w]{3})$")
                        .assign((t, v) -> processTaxEntries(t, v, type))
        
                        // Kirchensteuer auf Kapitalertragsteuer EUR -1,23
                        .section("tax", "currency").optional()
                        .match("^Kirchensteuer [\\d,.]+% auf [.,\\d]+ [\\w]{3} (?<tax>[.,\\d]+)- (?<currency>[\\w]{3})$")
                        .assign((t, v) -> processTaxEntries(t, v, type))

                        // Solidaritätszuschlag auf Kapitalertragsteuer EUR -6,76
                        .section("tax", "currency").optional()
                        .match("^Solidarit.tszuschlag [\\d,.]+% auf [.,\\d]+ [\\w]{3} (?<tax>[.,\\d]+)- (?<currency>[\\w]{3})$")
                        .assign((t, v) -> processTaxEntries(t, v, type));
    }

    private <T extends Transaction<?>> void addFeesSectionsTransaction(T transaction, DocumentType type)
    {
        transaction
                        // Provision 39,95- EUR
                        .section("fee", "currency").optional()
                        .match("^.*Provision\\s+(?<fee>[.,\\d\\s]+)- (?<currency>[\\w]{3}).*")
                        .assign((t, v) -> processFeeEntries(t, v, type))

                        // Abwicklungskosten Börse 0,04- EUR
                        .section("fee", "currency").optional()
                        .match("^Abwicklungskosten B.rse (?<fee>[.,\\d]+)- (?<currency>[\\w]{3}).*")
                        .assign((t, v) -> processFeeEntries(t, v, type))

                        // Transaktionsentgelt Börse 11,82- EUR
                        .section("fee", "currency").optional()
                        .match("^Transaktionsentgelt B.rse (?<fee>[.,\\d]+)- (?<currency>[\\w]{3}).*")
                        .assign((t, v) -> processFeeEntries(t, v, type))

                        // Übertragungs-/Liefergebühr 0,65- EUR
                        .section("fee", "currency").optional()
                        .match("^.bertragungs-\\/Liefergeb.hr (?<fee>[.,\\d]+)- (?<currency>[\\w]{3}).*")
                        .assign((t, v) -> processFeeEntries(t, v, type));
    }

    private void addTax(DocumentType type, Object t, Map<String, String> v, String taxtype)
    {
        // Wenn es 'Einbehaltene Quellensteuer' gibt, dann die weiteren
        // Quellensteuer-Arten nicht berücksichtigen.
        if (checkWithholdingTax(type, taxtype))
        {
            ((name.abuchen.portfolio.model.Transaction) t).addUnit(new Unit(Unit.Type.TAX,
                            Money.of(asCurrencyCode(v.get("currency")), asAmount(v.get(taxtype)))));
        }
    }
    
    

    private boolean checkWithholdingTax(DocumentType type, String taxtype)
    {
        if (Boolean.valueOf(type.getCurrentContext().get(FLAG_WITHHOLDING_TAX_FOUND)))
            return !"quellenstanr".equalsIgnoreCase(taxtype);
        return true;
    }

    private void processTaxEntries(Object t, Map<String, String> v, DocumentType type)
    {
        if (t instanceof name.abuchen.portfolio.model.Transaction)
        {
            Money tax = Money.of(asCurrencyCode(v.get("currency")), asAmount(v.get("tax")));
            PDFExtractorUtils.checkAndSetTax(tax, (name.abuchen.portfolio.model.Transaction) t, type);
        }
        else
        {
            Money tax = Money.of(asCurrencyCode(v.get("currency")), asAmount(v.get("tax")));
            PDFExtractorUtils.checkAndSetTax(tax, ((name.abuchen.portfolio.model.BuySellEntry) t).getPortfolioTransaction(), type);
        }
    }
    
    private void processFeeEntries(Object t, Map<String, String> v, DocumentType type)
    {
        if (t instanceof name.abuchen.portfolio.model.Transaction)
        {
            Money fee = Money.of(asCurrencyCode(v.get("currency")), asAmount(v.get("fee").replaceAll("\\s", "")));
            PDFExtractorUtils.checkAndSetFee(fee, (name.abuchen.portfolio.model.Transaction) t, type);
        }
        else
        {
            Money fee = Money.of(asCurrencyCode(v.get("currency")), asAmount(v.get("fee")));
            PDFExtractorUtils.checkAndSetFee(fee,
                            ((name.abuchen.portfolio.model.BuySellEntry) t).getPortfolioTransaction(), type);
        }
    }
}
