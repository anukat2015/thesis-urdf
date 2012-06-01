create table rel_stats as (
select f1.relation, f1.n, f2.mult1, f3.mult2 from
(select relation, count(id) as n from facts group by relation) f1,
(select relation, avg(n1) as mult1 from (select relation, arg1, count(id) as n1 from facts group by relation, arg1) group by relation) f2,
(select relation, avg(n2) as mult2 from (select relation, arg2, count(id) as n2 from facts group by relation, arg2) group by relation) f3
 where f1.relation = f2.relation and f1.relation = f3.relation
);

create index rel_stats_idx on rel_stats(relation);
create index facts_a1_rel_a2_c_idx on facts(arg1,relation,arg2,confidence);
create index facts_a2_rel_a1_c_idx on facts(arg2,relation,arg1,confidence);
create index facts_rel_a1_a2_c_idx on facts(relation,arg1,arg2,confidence);
create index facts_a1_a2_rel_c_idx on facts(arg1,arg2,relation,confidence);