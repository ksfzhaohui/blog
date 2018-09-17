-- 限流 key
local key = KEYS[1]
-- 限流大小
local limit = tonumber(ARGV[1])

local current = tonumber(redis.call('get',key) or "0")

if current + 1 > limit then
    return 0;
else  --请求值+1，并设置2秒过期
    redis.call("INCRBY", key, 1)
    redis.call("EXPIRE", key, 2)
    return current + 1
end