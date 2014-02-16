CREATE KEYSPACE mailstats WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

CREATE TABLE mailstats.SentFrom (
  fromDomain varchar, 
  server varchar,
  queue varchar, 
  time timestamp,
  sent int,
  PRIMARY KEY ((fromDomain,server, queue),time)
);

