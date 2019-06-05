import sys
import time
import boto3

def get_sqs():
    while True:
        try:
            sqs = boto3.resource(service_name="sqs", endpoint_url="http://localstack:4576")
            return sqs
        except:
            time.sleep(1)

def get_queue(sqs, name):
    while True:
        try:
            queue = sqs.get_queue_by_name(QueueName=name)
            return queue
        except:
            time.sleep(1)

if __name__ == "__main__":
    queue_a_name = sys.argv[1]
    queue_b_name = sys.argv[2]

    sqs = get_sqs()

    queue_a = get_queue(sqs, queue_a_name)
    queue_b = get_queue(sqs, queue_b_name)

    while True:
        for message in queue_a.receive_messages():
            x = str(int(message.body) + 1)
            print("x =", x, flush=True)
            message.delete()
            queue_b.send_message(MessageBody=x)