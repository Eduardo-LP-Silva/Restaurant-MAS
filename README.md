# Restaurant-MAS

## Overview

A restaurant environment portrayed by a multi-agent system based on JADE, developed for the AIAD course unit.

In this system, we have three different types of agents: Kitchen, Waiter and Customer. There is only one Kitchen, but multiple Waiters and Customers.

As a regular restaurant would work, the customers interact with the waiters to get their order done and the waiters interact with the kitchen and with other waiters as a way to get information quickly. The kitchen verifies the orders’ availability and takes care of the order preparation.

All of these interactions are implemented in accordance to either the FIPA-Request or FIPA-Contract-Net protocols.

## Used strategies
When a waiter doesn’t have information about a particular dish, it can choose to ask another waiter or the kitchen staff.

The first way is “quicker”, however, the information received might not be entirely accurate (either on purpose or not).

On the other hand, when asking the kitchen staff the information relayed is always up to date but “it takes longer” do this, dropping the customer’s mood.

There are two main strategies waiters can apply regarding information sharing:

1. Full cooperation - When asked about a certain dish, a waiter will, whenever possible, provide the information he believes to be most accurate.
2. Lying - In this strategy, wether a waiter actually possesses information about the requested dish or not, he will reply with possibly false information about it, making it look like the dish is worse than it actually is in order to make the other waiter suggest another one, thus not decreasing the availability of that dish.

## Usage
You can run a simulation using the command:

java jade.Boot -agents <agent_name>:<agent_class>[(<agent_arguments>)]

The waiters take a boolean as argument (true or false), indicating if he is a liar or not.

You can also run simulations based on files with the following structure (one line per agent type):

<agent_type> <number_of_agents>

You can do this with the command:

java app.Restaurant <file_name>

Developed by:
- Eduardo Silva up201603135@fe.up.pt
- Joana Ramos up201605017@fe.up.pt
- Pedro Gonçalves up201604643@fe.up.pt
