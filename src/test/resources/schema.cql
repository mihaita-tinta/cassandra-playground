CREATE KEYSPACE IF NOT EXISTS junit WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 1 };
USE junit;
CREATE TABLE IF NOT EXISTS account (id UUID PRIMARY KEY,
                                    name TEXT);
