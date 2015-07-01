#EMC Rupee Transaction Downloader

A Java library for downloading rupee transactions from your [Rupee History](http://empireminecraft.com/rupees/transactions) page.

```java
//read the first 100 transactions
String username = ...
String password = ...
int max = 100;
try (RupeeTransactionReader reader = new RupeeTransactionReader.Builder(username, password).build()) {
  for (int i = 1; i <= 100; i++) {
    RupeeTransaction transaction = reader.next();
    if (transaction == null) break;
    System.out.println(i + ". " + transaction);
  }
}
```
