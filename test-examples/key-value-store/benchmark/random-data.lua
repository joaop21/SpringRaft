-- Initialize the pseudo random number generator
math.randomseed(os.time())
math.random(); math.random(); math.random()


-- Generate a random UUID
function uuid()
  local seed={'e','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'}
  local tb={}
  for i=1,32 do
      table.insert(tb,seed[math.random(1,16)])
  end
  local sid=table.concat(tb)
  return string.format('%s-%s-%s-%s-%s',
      string.sub(sid,1,8),
      string.sub(sid,9,12),
      string.sub(sid,13,16),
      string.sub(sid,17,20),
      string.sub(sid,21,32)
  )
end


-- Ports for the servers to test
local ports = {8001,8002,8003}
-- Possible addresses to connect
local addrs = {}
-- counter for distribute addresses
local counter = 0


-- Thread setup: Decide which server to connect
function setup(thread)
   local append = function(host, port)
      for i, addr in ipairs(wrk.lookup(host, port)) do
         if wrk.connect(addr) then
            addrs[#addrs+1] = addr
         end
      end
   end

   if #addrs == 0 then
      for i=1, #ports, 1 do
        append("localhost", ports[i])
      end
   end

   local index = counter % #addrs + 1
   counter = counter + 1
   thread.addr = addrs[index]

   thread:set("uuid", uuid())
end


-- Thread init: Create the needed requests
function init(args)
  requests = {}
  req_counter = 0

  for i=1, 100, 1 do
    table.insert(requests, wrk.format("PUT", "/v2/keys/" .. uuid, {}, "value=" .. i))
    table.insert(requests, wrk.format("GET", "/v2/keys/" .. uuid))
  end
  table.insert(requests, wrk.format("DELETE", "/v2/keys/" .. uuid))

end

-- Thread request: Invoke requests
function request()
  if req_counter < #requests - 1 then
    req_counter = req_counter + 1
    return requests[req_counter]
  else
    req_counter = 0
    return requests[#requests]
  end
end
