# EMC Rupee Transaction Downloader

A Java library for downloading rupee transactions from your [Rupee History](http://empireminecraft.com/rupees/transactions) page.

try (RupeeTransactionReader reader = new RupeeTransactionReader.Builder("username", "password").build()) {
  RupeeTransaction transaction;
  int max = 10;
  int count = 0;
  while (count < max && (transaction = reader.next()) != null) {
    count++;
    System.out.println(count + ". " + transaction);
  }
}
