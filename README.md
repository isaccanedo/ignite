# Apache Ignite

<a href="https://ignite.apache.org/"><img src="https://github.com/apache/ignite-website/blob/master/assets/images/apache_ignite_logo.svg" hspace="20"/></a>

[![Build Status](https://travis-ci.org/apache/ignite.svg?branch=master)](https://travis-ci.org/apache/ignite)
[![GitHub](https://img.shields.io/github/license/apache/ignite?color=blue)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.ignite/ignite-core/badge.svg)](https://search.maven.org/search?q=org.apache.ignite)
[![GitHub release](https://img.shields.io/badge/release-download-brightgreen.svg)](https://ignite.apache.org/download.cgi)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/apache/ignite)
[![Twitter Follow](https://img.shields.io/twitter/follow/ApacheIgnite?style=social)](https://twitter.com/ApacheIgnite)

## O que é o Apache Ignite?

O Apache Ignite é um banco de dados distribuído para computação de alto desempenho com velocidade na memória.

<p align="center">
    <a href="https://ignite.apache.org">
        <img src="https://github.com/apache/ignite-website/blob/master/docs/2.9.0/images/ignite_clustering.png" width="400px"/>
    </a>
</p>

* [Technical Documentation](https://ignite.apache.org/docs/latest/)
* [JavaDoc](https://ignite.apache.org/releases/latest/javadoc/)
* [C#/.NET APIs](https://ignite.apache.org/releases/latest/dotnetdoc/api/)
* [C++ APIs](https://ignite.apache.org/releases/latest/cppdoc/)

## Armazenamento de várias camadas

O Apache Ignite foi projetado para funcionar com memória, disco e Intel Optane como níveis de armazenamento ativos. A camada de memória permite usar DRAM e Intel® Optane™ operando no modo de memória para armazenamento de dados e necessidades de processamento.A camada de disco é opcional com o suporte de duas opções -- você pode persistir os dados em um banco de dados externo ou mantê-los na persistência nativa do Ignite. SSD, Flash, HDD ou Intel Optane operando no AppDirect Mode podem ser usados como um dispositivo de armazenamento.

[consulte Mais informação](https://ignite.apache.org/arch/multi-tier-storage.html)

## Ignite Persistência Nativa

Embora o Apache Ignite seja amplamente usado como uma camada de cache sobre bancos de dados externos, ele vem com sua persistência nativa - um armazenamento baseado em disco distribuído, compatível com ACID e SQL.A persistência nativa se integra ao armazenamento multicamada do Ignite como uma camada de disco que pode ser ativada para permitir que o Ignite armazene mais dados no disco do que pode armazenar em cache na memória e permitir reinicializações rápidas do cluster.

[consulte Mais informação](https://ignite.apache.org/arch/persistence.html)

## Conformidade ACID
Os dados armazenados no Ignite são compatíveis com ACID tanto na memória quanto no disco, tornando o Ignite um sistema **fortemente consistente**. As transações do Ignite funcionam em toda a rede e podem abranger vários servidores.

[consulte Mais informação](https://ignite.apache.org/features/transactions.html)

## Suporte SQL ANSI
O Apache Ignite vem com um mecanismo SQL compatível com ANSI-99, horizontalmente escalonável e tolerante a falhas que permite que você interaja com o Ignite como se fosse um banco de dados SQL regular usando JDBC, drivers ODBC ou APIs SQL nativas disponíveis para Java, C#, C++, Python e outras linguagens de programação. O Ignite suporta todos os comandos DML, incluindo consultas SELECT, UPDATE, INSERT e DELETE, bem como um subconjunto de comandos DDL relevantes para sistemas distribuídos.

[consulte Mais informação](https://ignite.apache.org/features/sql.html)

## Aprendizado de máquina e computação de alto desempenho
[Apache Ignite Machine Learning](https://ignite.apache.org/features/machinelearning.html) é um conjunto de ferramentas simples, escaláveis e eficientes que permitem a construção de modelos preditivos de aprendizado de máquina sem transferências de dados dispendiosas. A justificativa para adicionar machine e deep learning ao Apache Ignite é bastante simples. Os cientistas de dados de hoje precisam lidar com dois fatores principais que impedem a adoção do ML.

A computação de alto desempenho (HPC) é a capacidade de processar dados e realizar cálculos complexos em altas velocidades. Usando o Apache Ignite como um [cluster de computação de alto desempenho](https://ignite.apache.org/use-cases/hpc.html), você pode transformar um grupo de máquinas comuns ou um ambiente de nuvem em um supercomputador distribuído de Nós de ignição. O Ignite permite velocidade e escala processando registros na memória e reduzindo a utilização da rede com APIs para dados e cálculos com uso intensivo de computação. Essas APIs implementam o paradigma MapReduce e permitem executar tarefas arbitrárias no cluster de nós.

