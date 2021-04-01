from locust import task, between
from locust.contrib.fasthttp import FastHttpUser
import uuid

class KeyValueClient(FastHttpUser):

  @task
  def work(self):
    key = str(uuid.uuid4())
    
    for value in range(1,101):
      self.client.put("/keys/"+key, data="value="+str(value))
      self.client.get("/keys/"+key)

    self.client.delete("/keys/"+key)
