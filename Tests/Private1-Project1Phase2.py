from http import HTTPStatus
from threading import Thread
import requests

# Check if only one order is assigned when multiple concurrent requests
# for order comes, when only one delivery agent is available

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082


def t1(result, order_id):  # First concurrent request

    # Customer 301 requests an order of item 1, quantity 3 from restaurant 101
    http_response = requests.post(
        "http://localhost:8081/orderDelivered", json={"orderId":order_id})

    result["1"] = http_response


def t2(result, order_id):  # Second concurrent request

    # Customer 302 requests an order of item 1, quantity 3 from restaurant 101
    http_response = requests.post(
        "http://localhost:8081/orderDelivered", json={"orderId":order_id})

    result["2"] = http_response


def test():

    result = {}

    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8084/reInitialize")

    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")

    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8086/reInitialize")

    # Agent 201 sign in
    http_response = requests.post(
        "http://localhost:8081/agentSignIn", json={"agentId": 201})

    if(http_response.status_code != HTTPStatus.CREATED):
        return 'Fail1'
        
    
    
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId": 2, "qty": 2})

    if(http_response.status_code != HTTPStatus.CREATED):
        return 'Fail1'

    order_id = http_response.json().get("orderId")
    ### Parallel Execution Begins ###
    thread1 = Thread(target=t1, kwargs={"result": result, "order_id":order_id})
    thread2 = Thread(target=t2, kwargs={"result": result, "order_id":order_id})

    thread1.start()
    thread2.start()

    thread1.join()
    thread2.join()

    ### Parallel Execution Ends ###
    status_code1 = result["1"].status_code
    status_code2 = result["2"].status_code
    
    if(status_code1 != HTTPStatus.CREATED and status_code2 != HTTPStatus.CREATED):
        return "Fail90"

    # Check status of first order
    http_response = requests.get(
        f"http://localhost:8081/order/{order_id}")

    if(http_response.status_code != HTTPStatus.OK):
        return 'Fail3'

    res_body = http_response.json()

    agent_id = res_body.get("agentId")
    order_status = res_body.get("status")

    if order_status != 'delivered' and agent_id != 201:
       return "Fail91"

    return 'Pass'


if __name__ == "__main__":

    print(test())
