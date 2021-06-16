from locust import task, TaskSet
from locust.contrib.fasthttp import FastHttpUser
import uuid

class APICalls(TaskSet):

  @task
  def work(self):
    key = str(uuid.uuid4())

    for value in range(1,101):
      self.client.put("/keys/"+key, data="value="+str(value))
      self.client.get("/keys/"+key)

    self.client.delete("/keys/"+key)


class KeyValueClient(FastHttpUser):
  host = 'http://localhost:8001/v2'
  tasks = [APICalls]