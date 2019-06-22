FROM python:3.7
WORKDIR .
ENV AWS_DEFAULT_REGION cn-north-1
ENV AWS_ACCESS_KEY_ID aws_access_key_id
ENV AWS_SECRET_ACCESS_KEY aws_secret_access_key
RUN pip install boto3
COPY . .
