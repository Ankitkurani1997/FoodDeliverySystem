Doker run command:-

Delivery :- docker run --net="host" -v /{Path_to_initialData.txt_file}/initialData.txt:/initialData.txt delivery

Restaurant :- docker run -p 8080:8080 -v /{Path_to_initialData.txt_file}/initialData.txt:/initialData.txt restaurant

Wallet :- docker run -p 8082:8082 -v /{Path_to_initialData.txt_file}/initialData.txt:/initialData.txt wallet 

Note :- delivery, restaurant and wallet are the docker image name

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

End-Points in Restaurant Service
1. POST /acceptOrder
with JSON payload of the form {“restId”: num, “itemId”: x, “qty”: y}
This end-point will be invoked by the Delivery service.
If the restaurant with ID num has at least y quantities of itemId x in its current
inventory
then
reduce the inventory of item x in restaurant num by yreturn HTTP status code 201 (created)
else
return HTTP status code 410 (gone)
2. POST /refillItem
with JSON payload of the form {“restId”: num, “itemId”: x, “qty”: y}
Increases the inventory of itemId x in restaurant num by y (provided num supplies x
as per /initialData.txt). Should return status code 201 unconditionally.
3. POST /reInitialize
Set inventory of all items in all restaurants as given in the /initialData.txt file.
Return HTTP status code 201.
End-points in Delivery Service
Keeps track of the status of each order placed so far. The status of any order can be either
unassigned, assigned, or delivered (unassigned means not yet given to a delivery agent).
For any assigned or delivered order, the service also records the agentId to which this order
is assigned. The service also keeps track of the current status of each delivery agent
(signed-out, unavailable, available). Maintains information about the price of each itemId in
each restaurant (this is constant information).
1. POST /requestOrder
with JSON payload of the form {“custId”: num, “restId”: x, “itemId”: y, “qty”: z}
This is the main end-point used by the customer to place an order. It first
computes the total billing amount for this order (as it maintains the price of
each itemId in each restaurant). It tries to deduct the amount from the custId
num’s wallet. If deduction does not succeed, return HTTP status code 410. If
the deduction succeeds, invoke the Restaurant Service’s acceptOrder
end-point, passing to it x, y, z. If that returns 410, restore the wallet balance
and return HTTP status code 410. Otherwise, first generate a fresh orderId w
(let orderIds start from 1000 and let them be incremented by one each time).
Initialize the status of this fresh order w as unassigned. Then see if any
Delivery Agent is available right now. If yes, assign an agentId to this order w
from among the available agentIds, update the status of the orderId w to
assigned, mark the assigned agentId as unavailable, and record that w is
assigned to this agentId. (If many agents are available, choose the lowest
numbered agentId among them.) Then, whether or not the orderId was
assigned an agent, return HTTP status code 201 and return response body
{“orderId”: w}.2. POST /agentSignIn
with JSON payload of the form {“agentId”: num}, where num is an agentId
If num is already in available or unavailable state, do nothing. Otherwise, If any
orderIds are currently in unassigned state, find the least numbered orderId y that is
unassigned, mark num as unavailable, mark the status of y as assigned, and record
that y is assigned to num. Otherwise, mark the status of num as available. In all
cases return HTTP status code 201.
3. POST /agentSignOut
with JSON payload of the form {“agentId”: num}, where num is an agentId
If num is already signed-out or is unavailable, do nothing, else mark num as being in
signed-out state. In both cases, return HTTP status code 201.
4. POST /orderDelivered
with JSON payload of the form {"orderId": num}, where num is an orderId
Ignore the request if num is not an orderId in assigned state. Otherwise, mark the
status of orderId num as delivered, and mark the status of the agentId who was
assigned this order as available. Let x be this agendId. If any orderIds are currently
in unassigned state, find the least numbered orderId y that is unassigned, mark x as
unavailable again, mark y as assigned, and record that y is assigned to x. In all cases
return 201.
5. GET /order/num
where num is an orderId.
If num is a non-existent orderId return HTTP status code 404. Otherwise return
status code 200 along with response JSON of the form {"orderId": num, "status": x,
“agentId”: y}, where x is unassigned, or assigned, or delivered. y will be the agentId
that is assigned the order num in case num is in assigned or delivered state, else y
will be -1.
6. GET /agent/num
where num is an agentId
Return status code 200 and response JSON of the form {"agendId": num, "status": y},
where y is signed-out, available, or unavailable.
7. POST /reInitializeDelete all orders from the records (no matter what their status is), and mark status of
each agent as signed-out. Return HTTP status code 201.
End-points in Wallet service
1. POST /addBalance
with JSON payload of the form {"custId": num, "amount": z}
To be invoked by Delivery Service.
Increase the balance of custId num by z, and return HTTP status code 201.
2. POST /deductBalance
with JSON payload of the form {"custId": num, "amount": z}
To be invoked by Delivery Service.
If current balance of custId num is less than z, return HTTP status code 410, else
reduce custId num's balance by z and return HTTP status code 201.
3. GET /balance/num
where num is a custId. Return HTTP status code 200, and response JSON of the
form {“custId”: num, “balance”: z}, where z is the current balance of custId num.
4. POST /reInitialize
Set balance of all customers to the initial value as given in the /initialData.txt file.
Return HTTP status code 201.
