from http import HTTPStatus
from threading import Thread
import requests

# Check if only one order is assigned when multiple concurrent requests
# for order comes, when only one delivery agent is available

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082


def t1(result, agent_id):  # First concurrent request

    # Customer 301 requests an order of item 1, quantity 3 from restaurant 101
    http_response = requests.post(
        "http://localhost:8081/agentSignOut", json={"agentId":agent_id})

    result["1"] = http_response


def t2(result, agent_id):  # Second concurrent request

    # Customer 302 requests an order of item 1, quantity 3 from restaurant 101
    http_response = requests.post(
        "http://localhost:8081/agentSignOut", json={"agentId":agent_id})

    result["2"] = http_response

def t3(result, agent_id):  # Second concurrent request

    # Customer 302 requests an order of item 1, quantity 3 from restaurant 101
    http_response = requests.post(
        "http://localhost:8081/agentSignOut", json={"agentId":agent_id})

    result["3"] = http_response

def t4(result, agent_id):  # Second concurrent request

    # Customer 302 requests an order of item 1, quantity 3 from restaurant 101
    http_response = requests.post(
        "http://localhost:8081/agentSignOut", json={"agentId":agent_id})

    result["4"] = http_response



def test():

    result = {}

    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8084/reInitialize")

    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")

    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8086/reInitialize")
        
        
    http_response = requests.post("http://localhost:8081/agentSignIn", json={"agentId":201})
    if(http_response.status_code != HTTPStatus.CREATED):
        return "Fail"
    
    http_response = requests.post("http://localhost:8081/agentSignIn", json={"agentId":202})
    if(http_response.status_code != HTTPStatus.CREATED):
        return "Fail"
    ### Parallel Execution Begins ###
    thread1 = Thread(target=t1, kwargs={"result":result, "agent_id":201})
    thread2 = Thread(target=t2, kwargs={"result":result, "agent_id":202})
    thread3 = Thread(target=t3, kwargs={"result":result, "agent_id":201})
    thread4 = Thread(target=t4, kwargs={"result":result, "agent_id":202})

    thread1.start()
    thread2.start()
    thread3.start()
    thread4.start()

    thread1.join()
    thread2.join()
    thread3.join()
    thread4.join()
    ### Parallel Execution Ends ###
    status_code1 = result["1"].status_code
    status_code2 = result["2"].status_code
    status_code3 = result["3"].status_code
    status_code4 = result["4"].status_code
    
    if(status_code1 != HTTPStatus.CREATED and status_code2 != HTTPStatus.CREATED and status_code4 != HTTPStatus.CREATED and status_code3 != HTTPStatus.CREATED):
        return "Fail90"


    return 'Pass'


if __name__ == "__main__":

    print(test())
