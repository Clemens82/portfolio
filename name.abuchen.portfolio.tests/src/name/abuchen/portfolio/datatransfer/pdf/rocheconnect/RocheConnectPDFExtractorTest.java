package name.abuchen.portfolio.datatransfer.pdf.rocheconnect;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import name.abuchen.portfolio.datatransfer.Extractor.BuySellEntryItem;
import name.abuchen.portfolio.datatransfer.Extractor.Item;
import name.abuchen.portfolio.datatransfer.Extractor.SecurityItem;
import name.abuchen.portfolio.datatransfer.Extractor.TransactionItem;
import name.abuchen.portfolio.datatransfer.actions.AssertImportActions;
import name.abuchen.portfolio.datatransfer.pdf.PDFInputFile;
import name.abuchen.portfolio.datatransfer.pdf.RocheConnectPDFExtractor;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.BuySellEntry;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.Transaction.Unit;
import name.abuchen.portfolio.money.Money;
import name.abuchen.portfolio.money.Values;

@SuppressWarnings("nls")
public class RocheConnectPDFExtractorTest
{
    @Test
    public void testWertpapierKauf01()
    {
        RocheConnectPDFExtractor extractor = new RocheConnectPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "rocheconnect_kauf01.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(4));
        System.out.println("Results: " + results.size());

        // check security
        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst()
                        .orElseThrow(IllegalArgumentException::new).getSecurity();
        assertThat(security.getIsin(), is("CH0012032048"));
        

        // check only first buy sell transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.orElseThrow(IllegalArgumentException::new).getSubject(), instanceOf(BuySellEntry.class));
        BuySellEntry entry = (BuySellEntry) item.orElseThrow(IllegalArgumentException::new).getSubject();
        System.out.println(entry.getPortfolioTransaction().toString());

        assertThat(entry.getPortfolioTransaction().getType(), is(PortfolioTransaction.Type.BUY));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.BUY));

        assertThat(entry.getPortfolioTransaction().getAmount(), is(Values.Amount.factorize(567.1375512)));
        assertThat(entry.getPortfolioTransaction().getDateTime(), is(LocalDateTime.parse("2021-07-19T00:00:00")));
        assertThat(entry.getPortfolioTransaction().getShares(), is(Values.Share.factorize(1.58631)));
        
    }

    @Test
    public void testWertpapierKauf02()
    {
        RocheConnectPDFExtractor extractor = new RocheConnectPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();
        
        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "rocheconnect_kauf03-MÄR.txt"), errors);
        assertThat(errors, empty());
        assertThat(results.size(), is(6));

        results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "rocheconnect_kauf04-NPE.txt"), errors);
        assertThat(errors, empty());
        assertThat(results.size(), is(4));


        results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "rocheconnect_kauf02.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(4));
        System.out.println("Results: " + results.size());

        // check security
        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst()
                        .orElseThrow(IllegalArgumentException::new).getSecurity();
        assertThat(security.getIsin(), is("CH0012032048"));
        

        // check only first buy sell transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.orElseThrow(IllegalArgumentException::new).getSubject(), instanceOf(BuySellEntry.class));
        BuySellEntry entry = (BuySellEntry) item.orElseThrow(IllegalArgumentException::new).getSubject();
        System.out.println(entry.getPortfolioTransaction().toString());

        assertThat(entry.getPortfolioTransaction().getType(), is(PortfolioTransaction.Type.BUY));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.BUY));

        assertThat(entry.getPortfolioTransaction().getAmount(), is(Values.Amount.factorize(299.588488318)));
        assertThat(entry.getPortfolioTransaction().getDateTime(), is(LocalDateTime.parse("2014-10-20T00:00")));
        assertThat(entry.getPortfolioTransaction().getShares(), is(Values.Share.factorize(1.118180)));
        
    }

    @Test
    public void testWertpapierKauf03()
    {
        RocheConnectPDFExtractor extractor = new RocheConnectPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "RocheConnect_kauf03.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));

        // check security
        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst()
                        .orElseThrow(IllegalArgumentException::new).getSecurity();
        assertThat(security.getIsin(), is("IE00B3RBWM25"));
        assertThat(security.getWkn(), is("A1JX52"));
        assertThat(security.getName(), is("VANGUARD FTSE ALL-WORLD U.ETF REGISTERED SHARES USD DIS.ON"));

        // check buy sell transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.orElseThrow(IllegalArgumentException::new).getSubject(), instanceOf(BuySellEntry.class));
        BuySellEntry entry = (BuySellEntry) item.orElseThrow(IllegalArgumentException::new).getSubject();

        assertThat(entry.getPortfolioTransaction().getType(), is(PortfolioTransaction.Type.BUY));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.BUY));

        assertThat(entry.getPortfolioTransaction().getAmount(), is(Values.Amount.factorize(299.588488318)));
        assertThat(entry.getPortfolioTransaction().getDateTime(), is(LocalDateTime.parse("2014-10-20T00:00")));
        assertThat(entry.getPortfolioTransaction().getShares(), is(Values.Share.factorize(1.118180)));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.FEE),
                        is(Money.of("EUR", Values.Amount.factorize(0.90))));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.TAX),
                        is(Money.of("EUR", Values.Amount.factorize(0.00))));
    }

    @Test
    public void testPRIVATE()
    {
        RocheConnectPDFExtractor extractor = new RocheConnectPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "RocheConnect_keepPrivate.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(28));

        Iterator<Item> it = results.stream().filter(i -> i instanceof BuySellEntryItem).iterator();
        while (it.hasNext()) {
            // 16 Kauf / Verkauf
            // 3 Dividendengutschrift
            Item item = it.next();
            
            BuySellEntry entry = (BuySellEntry) item.getSubject();
            
            System.out.println(entry.getPortfolioTransaction().getSecurity().getName());
            System.out.println(entry.getPortfolioTransaction().getDateTime());

        }
                        
        // check security
        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst()
                        .orElseThrow(IllegalArgumentException::new).getSecurity();
        assertThat(security.getIsin(), is("US02079K3059"));
        assertThat(security.getWkn(), is("A14Y6F"));

        // check buy sell transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.orElseThrow(IllegalArgumentException::new).getSubject(), instanceOf(BuySellEntry.class));
        BuySellEntry entry = (BuySellEntry) item.orElseThrow(IllegalArgumentException::new).getSubject();

        assertThat(entry.getPortfolioTransaction().getType(), is(PortfolioTransaction.Type.SELL));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));

        assertThat(entry.getPortfolioTransaction().getAmount(), is(Values.Amount.factorize(3306.21)));
        assertThat(entry.getPortfolioTransaction().getDateTime(), is(LocalDateTime.parse("2015-12-10T14:02:23")));
        assertThat(entry.getPortfolioTransaction().getShares(), is(Values.Share.factorize(5)));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.FEE),
                        is(Money.of("EUR", Values.Amount.factorize(14.95))));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.TAX),
                        is(Money.of("EUR", Values.Amount.factorize(0.00))));
    }
    
    @Test
    public void testWertpapierVerkauf01()
    {
        RocheConnectPDFExtractor extractor = new RocheConnectPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "RocheConnect_verkauf01.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));

        // check security
        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst()
                        .orElseThrow(IllegalArgumentException::new).getSecurity();
        assertThat(security.getIsin(), is("IE00BF2B0K52"));
        assertThat(security.getWkn(), is("A2DTF1"));
        assertThat(security.getName(), is("FRAN.LIBERTYQ EM.MAR.EQ.UC.ETF REGISTERED SHARES USD ACC.O.N."));

        // check buy sell transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.orElseThrow(IllegalArgumentException::new).getSubject(), instanceOf(BuySellEntry.class));
        BuySellEntry entry = (BuySellEntry) item.orElseThrow(IllegalArgumentException::new).getSubject();

        assertThat(entry.getPortfolioTransaction().getType(), is(PortfolioTransaction.Type.SELL));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));

        assertThat(entry.getPortfolioTransaction().getAmount(), is(Values.Amount.factorize(3141.58)));
        assertThat(entry.getPortfolioTransaction().getDateTime(), is(LocalDateTime.parse("2021-03-11T16:34:51")));
        assertThat(entry.getPortfolioTransaction().getShares(), is(Values.Share.factorize(137)));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.FEE), 
                        is(Money.of("EUR", Values.Amount.factorize(29.97))));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.TAX),
                        is(Money.of("EUR", Values.Amount.factorize(0.00))));
    }

    @Test
    public void testWertpapierVerkauf02()
    {
        RocheConnectPDFExtractor extractor = new RocheConnectPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "RocheConnect_verkauf02.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));

        // check security
        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst()
                        .orElseThrow(IllegalArgumentException::new).getSecurity();
        assertThat(security.getIsin(), is("US64110L1061"));
        assertThat(security.getWkn(), is("552484"));
        assertThat(security.getName(), is("NETFLIX INC. REGISTERED SHARES DL -,001"));

        // check buy sell transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.orElseThrow(IllegalArgumentException::new).getSubject(), instanceOf(BuySellEntry.class));
        BuySellEntry entry = (BuySellEntry) item.orElseThrow(IllegalArgumentException::new).getSubject();

        assertThat(entry.getPortfolioTransaction().getType(), is(PortfolioTransaction.Type.SELL));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));

        assertThat(entry.getPortfolioTransaction().getAmount(), is(Values.Amount.factorize(1094.73)));
        assertThat(entry.getPortfolioTransaction().getDateTime(), is(LocalDateTime.parse("2016-12-13T10:35:11")));
        assertThat(entry.getPortfolioTransaction().getShares(), is(Values.Share.factorize(10)));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.FEE), 
                        is(Money.of("EUR", Values.Amount.factorize(8.60))));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.TAX),
                        is(Money.of("EUR", Values.Amount.factorize(48.17))));
    }
    
    @Test
    public void testWertpapierVerkauf03()
    {
        RocheConnectPDFExtractor extractor = new RocheConnectPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "RocheConnect_verkauf03.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));

        // check security
        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst()
                        .orElseThrow(IllegalArgumentException::new).getSecurity();
        assertThat(security.getIsin(), is("US53578A1088"));
        assertThat(security.getWkn(), is("A1H82D"));
        assertThat(security.getName(), is("LINKEDIN CORP. REGISTERED SHS CL.A DL-,0001"));

        // check buy sell transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.orElseThrow(IllegalArgumentException::new).getSubject(), instanceOf(BuySellEntry.class));
        BuySellEntry entry = (BuySellEntry) item.orElseThrow(IllegalArgumentException::new).getSubject();

        assertThat(entry.getPortfolioTransaction().getType(), is(PortfolioTransaction.Type.SELL));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));

        assertThat(entry.getPortfolioTransaction().getAmount(), is(Values.Amount.factorize(1279.69)));
        assertThat(entry.getPortfolioTransaction().getDateTime(), is(LocalDateTime.parse("2016-12-13T00:00:00")));
        assertThat(entry.getPortfolioTransaction().getShares(), is(Values.Share.factorize(7)));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.FEE), 
                        is(Money.of("EUR", Values.Amount.factorize(9.95))));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.TAX),
                        is(Money.of("EUR", Values.Amount.factorize(0.00))));
    }
    
    @Test
    public void testWertpapierVerkauf04()
    {
        RocheConnectPDFExtractor extractor = new RocheConnectPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "RocheConnect_verkauf04.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));

        // check security
        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst()
                        .orElseThrow(IllegalArgumentException::new).getSecurity();
        assertThat(security.getIsin(), is("LU0274208692"));
        assertThat(security.getWkn(), is("DBX1MW"));
        assertThat(security.getName(), is("XTRACKERS MSCI WORLD SWAP INHABER-ANTEILE 1C O.N."));

        // check buy sell transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.orElseThrow(IllegalArgumentException::new).getSubject(), instanceOf(BuySellEntry.class));
        BuySellEntry entry = (BuySellEntry) item.orElseThrow(IllegalArgumentException::new).getSubject();

        assertThat(entry.getPortfolioTransaction().getType(), is(PortfolioTransaction.Type.SELL));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));

        assertThat(entry.getPortfolioTransaction().getAmount(), is(Values.Amount.factorize(49.01)));
        assertThat(entry.getPortfolioTransaction().getDateTime(), is(LocalDateTime.parse("2020-01-10T00:00:00")));
        assertThat(entry.getPortfolioTransaction().getShares(), is(Values.Share.factorize(0.7918)));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.FEE), 
                        is(Money.of("EUR", Values.Amount.factorize(0.00))));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.TAX),
                        is(Money.of("EUR", Values.Amount.factorize(0.18))));
    }

    @Test
    public void testDividende01()
    {
        Client client = new Client();

        RocheConnectPDFExtractor extractor = new RocheConnectPDFExtractor(client);

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "RocheConnect_dividende01.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, "EUR");

        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst().get().getSecurity();
        assertThat(security.getIsin(), is("US4781601046"));
        assertThat(security.getWkn(), is("853260"));
        assertThat(security.getName(), is("JOHNSON & JOHNSON  SHARES REGISTERED SHARES DL 1"));

        AccountTransaction t = (AccountTransaction) results.stream().filter(i -> i instanceof TransactionItem).findFirst()
                        .orElseThrow(IllegalArgumentException::new).getSubject();
        
        assertThat(t.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(t.getMonetaryAmount(), is(Money.of("EUR", Values.Amount.factorize(8.64))));
        assertThat(t.getShares(), is(Values.Share.factorize(12)));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2021-02-22T00:00")));
        assertThat(t.getSource(), is("RocheConnect_dividende01.txt"));
        assertThat(t.getNote(), is("Quartalsdividende"));

        assertThat(t.getGrossValue(), is(Money.of("EUR", Values.Amount.factorize(10.17))));
        assertThat(t.getUnitSum(Unit.Type.TAX), is(Money.of("EUR", Values.Amount.factorize(1.53))));
        assertThat(t.getUnitSum(Unit.Type.FEE), is(Money.of("EUR", Values.Amount.factorize(0.00))));
    }

    @Test
    public void testDividende02()
    {
        Client client = new Client();

        RocheConnectPDFExtractor extractor = new RocheConnectPDFExtractor(client);

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "RocheConnect_dividende02.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, "EUR");

        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst().get().getSecurity();
        assertThat(security.getIsin(), is("DE0006231004"));
        assertThat(security.getWkn(), is("623100"));
        assertThat(security.getName(), is("INFINEON TECHNOLOGIES AG NAMENS-AKTIEN O.N."));

        AccountTransaction t = (AccountTransaction) results.stream().filter(i -> i instanceof TransactionItem).findFirst()
                        .orElseThrow(IllegalArgumentException::new).getSubject();
        
        assertThat(t.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(t.getMonetaryAmount(), is(Money.of("EUR", Values.Amount.factorize(4.40))));
        assertThat(t.getShares(), is(Values.Share.factorize(20)));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2021-02-26T00:00")));

        assertThat(t.getGrossValue(), is(Money.of("EUR", Values.Amount.factorize(4.40))));
        assertThat(t.getUnitSum(Unit.Type.TAX), is(Money.of("EUR", Values.Amount.factorize(0.00))));
        assertThat(t.getUnitSum(Unit.Type.FEE), is(Money.of("EUR", Values.Amount.factorize(0.00))));
    }

    @Test
    public void testDividende03()
    {
        Client client = new Client();

        RocheConnectPDFExtractor extractor = new RocheConnectPDFExtractor(client);

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "RocheConnect_dividende03.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, "EUR");

        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst().get().getSecurity();
        assertThat(security.getIsin(), is("DE0005557508"));
        assertThat(security.getWkn(), is("555750"));
        assertThat(security.getName(), is("DEUTSCHE TELEKOM AG NAMENS-AKTIEN O.N."));

        AccountTransaction t = (AccountTransaction) results.stream().filter(i -> i instanceof TransactionItem).findFirst()
                        .orElseThrow(IllegalArgumentException::new).getSubject();
        
        assertThat(t.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(t.getMonetaryAmount(), is(Money.of("EUR", Values.Amount.factorize(68.40))));
        assertThat(t.getShares(), is(Values.Share.factorize(114)));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2021-04-06T00:00")));

        assertThat(t.getGrossValue(), is(Money.of("EUR", Values.Amount.factorize(68.40))));
        assertThat(t.getUnitSum(Unit.Type.TAX), is(Money.of("EUR", Values.Amount.factorize(0.00))));
        assertThat(t.getUnitSum(Unit.Type.FEE), is(Money.of("EUR", Values.Amount.factorize(0.00))));
    }
    
    @Test
    public void testDividende04()
    {
        Client client = new Client();

        RocheConnectPDFExtractor extractor = new RocheConnectPDFExtractor(client);

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "RocheConnect_dividende04.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, "EUR");

        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst().get().getSecurity();
        assertThat(security.getIsin(), is("DE000A0Q4R36"));
        assertThat(security.getWkn(), is("A0Q4R3"));
        assertThat(security.getName(), is("ISH.ST.EU.600 HEALT.C.U.ETF DE INHABER-ANLAGEAKTIEN"));

        AccountTransaction t = (AccountTransaction) results.stream().filter(i -> i instanceof TransactionItem).findFirst()
                        .orElseThrow(IllegalArgumentException::new).getSubject();
        
        assertThat(t.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(t.getMonetaryAmount(), is(Money.of("EUR", Values.Amount.factorize(19.25))));
        assertThat(t.getShares(), is(Values.Share.factorize(20)));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2016-04-15T00:00")));

        assertThat(t.getGrossValue(), is(Money.of("EUR", Values.Amount.factorize(19.54)))); // Why not 19.25? See https://github.com/buchen/portfolio/pull/2533
        assertThat(t.getUnitSum(Unit.Type.TAX), is(Money.of("EUR", Values.Amount.factorize(0.29))));
        assertThat(t.getUnitSum(Unit.Type.FEE), is(Money.of("EUR", Values.Amount.factorize(0.00))));
    }

    @Test
    public void testAusschuettung01()
    {
        Client client = new Client();

        RocheConnectPDFExtractor extractor = new RocheConnectPDFExtractor(client);

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "RocheConnect_ausschuettung01.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, "EUR");

        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst().get().getSecurity();
        assertThat(security.getIsin(), is("IE00B1FZS350"));
        assertThat(security.getWkn(), is("A0LEW8"));
        assertThat(security.getName(), is("ISHSII-DEV.MKTS PROP.YLD U.ETF REGISTERED SHS USD (DIST) O.N."));

        AccountTransaction t = (AccountTransaction) results.stream().filter(i -> i instanceof TransactionItem).findFirst()
                        .orElseThrow(IllegalArgumentException::new).getSubject();
        
        assertThat(t.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(t.getMonetaryAmount(), is(Money.of("EUR", Values.Amount.factorize(9.65))));
        assertThat(t.getShares(), is(Values.Share.factorize(81)));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2021-02-11T00:00")));

        assertThat(t.getGrossValue(), is(Money.of("EUR", Values.Amount.factorize(9.65))));
        assertThat(t.getUnitSum(Unit.Type.TAX), is(Money.of("EUR", Values.Amount.factorize(0.00))));
        assertThat(t.getUnitSum(Unit.Type.FEE), is(Money.of("EUR", Values.Amount.factorize(0.00))));
    }
}
