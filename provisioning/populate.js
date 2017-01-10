db.createCollection("sources");
db.createCollection("log");
db.createCollection("error");

db.getCollection("sources").insert([
{ url: "http://ipt.vliz.be/eurobis/rss.do" },
{ url: "http://ipt.vliz.be/obiscanada/rss.do" },
{ url: "https://nzobisipt.niwa.co.nz/rss.do" },
{ url: "http://ipt.biodiversity.aq/rss.do" },
{ url: "http://ipt.csir.co.za/ipt/rss.do" },
{ url: "http://ipt.iobis.org/caribbeanobis/rss.do" },
{ url: "http://ogc-act.csiro.au/ipt/rss.do" },
{ url: "http://www.iobis.org.cn:8080/ipt/rss.do" },
{ url: "https://www1.usgs.gov/obis-usa/ipt/rss.do" },
{ url: "http://ipt.iobis.org/arcod/rss.do" },
{ url: "http://ipt.iobis.org/seaobis/rss.do" },
{ url: "http://arobis.cenpat-conicet.gob.ar:8081/rss.do" },
{ url: "http://ipt.iobis.org/hab/rss.do" },
{ url: "http://ipt.iobis.org/indobis/rss.do" },
{ url: "http://geo.abds.is/ipt/rss.do" },
{ url: "http://ipt.iobis.org/obis-env/rss.do" },
{ url: "http://ipt.medobis.eu/rss.do" },
{ url: "http://ipt.iobis.org/obis-deepsea/rss.do" },
{ url: "http://gp.sea.gov.ua:8082/ipt/rss.do" },
{ url: "http://www.godac.jamstec.go.jp/ipt/rss.do" }
]);