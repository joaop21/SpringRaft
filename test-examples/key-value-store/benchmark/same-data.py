from locust import task, TaskSet
from locust.contrib.fasthttp import FastHttpUser

class APICalls(TaskSet):

  @task
  def work(self):
    key = 'benchmark-key'
    
    for value in range(1,101):
      self.client.put("/keys/"+key, data="value="+str(value))
      self.client.get("/keys/"+key)

    self.client.delete("/keys/"+key)


class KeyValueClient1(FastHttpUser):
  host = 'http://localhost:8001/v2'
  tasks = [APICalls]


class KeyValueClient2(FastHttpUser):
  host = 'http://localhost:8002/v2'
  tasks = [APICalls]


class KeyValueClient3(FastHttpUser):
  host = 'http://localhost:8003/v2'
  tasks = [APICalls]
