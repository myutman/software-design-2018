import boto3
import time

while True:
    try:
        sqs = boto3.resource(service_name="sqs", endpoint_url="http://localstack:4576")
        sqs.create_queue(QueueName="lol")
    except:
        time.sleep(0.228)
        continue
    break
        
print("halo! ave satan!")
