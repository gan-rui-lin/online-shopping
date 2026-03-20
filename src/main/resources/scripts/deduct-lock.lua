-- KEYS: stock hash keys per sku
-- ARGV: quantity per sku
-- Return 0 for success, or 1-based index of failed item
local function toNumber(val)
  if val == nil then
    return nil
  end
  if type(val) == "string" then
    val = string.gsub(val, '"', '')
  end
  return tonumber(val)
end

for i = 1, #KEYS do
  local key = KEYS[i]
  local count = toNumber(ARGV[i])
  local lock = toNumber(redis.call("HGET", key, "lock") or "0")
  if lock < count then
    return i
  end
end

for i = 1, #KEYS do
  local key = KEYS[i]
  local count = toNumber(ARGV[i])
  redis.call("HINCRBY", key, "lock", -count)
end

return 0
