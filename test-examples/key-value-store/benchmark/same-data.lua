-- Initialize the pseudo random number generator
math.randomseed(os.time())
math.random(); math.random(); math.random()


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
end


-- Thread init: Create the needed requests
function init(args)
  requests = {}
  req_counter = 0

  for i=1, 100, 1 do
    table.insert(requests, wrk.format("PUT", "/v2/keys/benchmark-key", {}, "value=" .. i))
    table.insert(requests, wrk.format("GET", "/v2/keys/benchmark-key"))
  end
  table.insert(requests, wrk.format("DELETE", "/v2/keys/benchmark-key"))

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
