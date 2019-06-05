import boto3

while True:
    try:
        sqs = boto3.resource(service_name="sqs", endpoint_url="http://localstack:4576")
        sqs.create_queue(QueueName="lol")
    except:
        sleep(0.228)
        continue
        
print("halo! ave satan!")
