import os
import time

import boto3
from botocore.config import Config
from botocore.exceptions import ClientError


def main():
    client = boto3.client("s3", endpoint_url=os.environ["S3_ENDPOINT"], region_name="us-east-1",
                           aws_access_key_id=os.environ["S3_ACCESS_KEY"],
                           aws_secret_access_key=os.environ["S3_SECRET_KEY"],
                           config=Config(connect_timeout=2, read_timeout=3, retries={"max_attempts": 0}))
    bucket = os.getenv("S3_BUCKET", "integritylens")
    for attempt in range(30):
        try:
            client.create_bucket(Bucket=bucket)
            return
        except ClientError as error:
            if error.response["Error"]["Code"] in ("BucketAlreadyOwnedByYou", "BucketAlreadyExists"):
                client.head_bucket(Bucket=bucket)
                return
            if attempt == 29:
                raise RuntimeError("Cannot initialize storage") from None
        except Exception:
            if attempt == 29:
                raise RuntimeError("Cannot initialize storage") from None
        time.sleep(2)


if __name__ == "__main__":
    main()
