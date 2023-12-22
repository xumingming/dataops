# DataOps

A simple program to run sql scripts against databases(currently MySQL).

## Build

```bash
mvn clean install
```

## Config

You need to have a config file at `~/.dataops.yaml`:

```
host: <db_host>
db: <db_name>
user: <user_name>
password: <password>
```

And you need to set the following environment variables:

```bash
export DATAOPS_HOME=<path-to-your-dataops-dir>
export PATH=$DATAOPS_HOME/bin:$PATH
```

## Usage

Current ability:

### Run sql with specified sqlFile and parallelism

```bash
dataops run <sqlPath> <parallelism>
```

Sample output:

```bash
➜ dataops run normal.sql 5
Host    : mysql.test.com
Db      : test_db
User    : test_user
Password: test_pass

[Thread4-0] Status: OK, RT: 1029ms
[Thread2-0] Status: OK, RT: 1030ms
[Thread4-1] Status: OK, RT: 716ms
[Thread2-1] Status: OK, RT: 975ms
[Thread4-2] Status: OK, RT: 812ms
[Thread2-2] Status: OK, RT: 901ms
[Thread3-2] Status: OK, RT: 850ms
```

### Run sql with placeholders

You can define placeholder in sql, e.g. we can define a placeholder named `id` like this:

```sql
select * from nation where n_nationkey = __id__
```

And when running, you can specify how to fill real value by:

```bash
dataops run test.sql 10 -p id:1,100 -v
```

`id:1,100` here means `__id__` in SQL will be replaced by a random value in range: `[1, 100)`, the effect is:

```bash
➜  poc dataops run test.sql 10 -p id:1,100 -v
Host    : mysql.test.com
Db      : test_db
User    : test_user
Password: test_pass

Thread 0 will run SQL: select * from nation where n_nationkey = 82
Thread 1 will run SQL: select * from nation where n_nationkey = 59
Thread 2 will run SQL: select * from nation where n_nationkey = 68
Thread 3 will run SQL: select * from nation where n_nationkey = 51
Thread 4 will run SQL: select * from nation where n_nationkey = 64
Thread 5 will run SQL: select * from nation where n_nationkey = 16
Thread 6 will run SQL: select * from nation where n_nationkey = 96
Thread 7 will run SQL: select * from nation where n_nationkey = 42
Thread 8 will run SQL: select * from nation where n_nationkey = 23
Thread 9 will run SQL: select * from nation where n_nationkey = 30
[Thread6-0] Status: OK, RT: 28ms
[Thread9-0] Status: OK, RT: 27ms
[Thread3-0] Status: OK, RT: 27ms
```

You can define more than one placeholders:

```sql
select * from nation where n_nationkey = __id__ and name='hello__name__'
```

Then run with:

```bash
dataops run test.sql 10 -p id:1,100;name:1,100 -v
```


### Show verbose log

```bash
dataops run test.sql 10 -v
```

Enjoy!

