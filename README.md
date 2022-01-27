The objective of this project is to develop a food delivery application (similar to Zomato,
Swiggy). The application will be organized as a set of three microservices, each hosting a
RESTful service. The high-level features of the application are as follows:

There is a fixed set of customers, restaurants, item IDs, and delivery agents,
specified initially in a given input file when the application starts. This input file is
called /initialData.txt, and is explained more in a section titled Initialization later in
this document.

The three services are Restaurant, Delivery, and Wallet.

Delivery is the main service, with which customers interact and which invokes the
other services in turn. The Delivery service keeps track of orders placed so far, the
current statuses of the orders, and the current statuses of the delivery agents. The
Delivery service is also aware of the price of each item in each restaurant (this is
constant and specified upfront). This way, the Delivery service is able to calculate the
total amount of an order when the order is received. An order can be for a single item
only (but any quantity of it).

The Restaurant service keeps track of the inventory of items available in all the
restaurants. The inventory reduces when an order is received, and can be increased
using a specified end-point.

The Wallet service keeps track of the balance maintained by each customer, and
supports end-points to decrease or increase wallet amounts.

Any delivery agent can sign-in whenever they like. Whenever they are in signed-in
state, they are either available (ready to deliver an order) or unavailable (i.e.,
currently delivering an order). They can sign-out whenever they like provided they
are in available state. The Delivery service can assign an order to an agent only if the
agent is in available state.

Restaurants are assumed to be always open and serving.

For any end-point below, if it is not mentioned who is to invoke it, then it is intended
to be invoked by a human (delivery agent, customer, restaurant manager, etc). In our
setting, a test-script will send requests to these end-points on behalf of humans.
