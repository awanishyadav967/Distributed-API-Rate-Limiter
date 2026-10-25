local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refillRate = tonumber(ARGV[2])

-- Read Redis server time in milliseconds
local time = redis.call('TIME')
local now = tonumber(time[1]) * 1000
        + math.floor(tonumber(time[2]) / 1000)

-- Load the bucket, or start with a full bucket
local stored = redis.call('HMGET', key, 'tokens', 'last_refill_ms')
local tokens = tonumber(stored[1]) or capacity
local lastRefill = tonumber(stored[2]) or now

-- Refill while respecting capacity
now = math.max(now, lastRefill)
local elapsedSeconds = (now - lastRefill) / 1000
tokens = math.min(capacity, tokens + elapsedSeconds * refillRate)

-- Try to consume one token
local allowed = 0
if tokens >= 1 then
    tokens = tokens - 1
    allowed = 1
end

-- Save the updated bucket
redis.call('HSET', key,
        'tokens', tostring(tokens),
        'last_refill_ms', tostring(now))

-- Remove inactive buckets after twice the full refill duration
local ttlSeconds = math.max(1, math.ceil(capacity / refillRate) * 2)
redis.call('EXPIRE', key, ttlSeconds)

return allowed