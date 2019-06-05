import boto3
import time

def create_queue(sqs, name):
    while True:
        try:
            queue = sqs.create_queue(QueueName=name)
        except:
            time.sleep(0.288)
            continue
        break
    return queue

def get_sqs():
    while True:
        try:
            sqs = boto3.resource(service_name="sqs", endpoint_url="http://localstack:4576")
        except:
            time.sleep(0.228)
            continue
        break
    return sqs

if __name__ == "__main__":
    sqs = get_sqs()
    queueA = create_queue(sqs, "A")
    queueB = create_queue(sqs, "B")
    queueA.send_message(MessageBody="1")
    print("halo! ave satan!")
