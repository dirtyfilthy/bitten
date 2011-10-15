require 'digest'
require 'active_record'
require 'mechanize'

class  Address < ActiveRecord::Base
  

  B58Chars = '123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz'
  B58Base = B58Chars.length


  def self.from_webpage(url)
    agent=Mechanize.new
    begin
      page=agent.get(url)
    rescue Mechanize::ResponseCodeError => e
      return []
    rescue Timeout::Error => e
      return []
    rescue Net::HTTP::Persistent::Error => e
      return []
    rescue SocketError => e
      return []
    rescue Errno::EHOSTUNREACH => e
      return []
    rescue Errno::ETIMEDOUT => e
      return []
    rescue Errno::ENETUNREACH => e
      return []
    rescue URI::InvalidURIError =>e
      return []
    rescue NoMethodError => e
      return []
    rescue Zlib::DataError => e
      return []
    rescue Zlib::GzipFile::NoFooter => e
      return []
    rescue URI::InvalidComponentError => e
      return []
    end
    addresses=page.body.scan(regexp)
    addresses=addresses.select {|a| valid_bitcoin_address?(a)}
    addresses.uniq!
    return addresses
  end

  def self.valid_bitcoin_address?(address)
    (address =~ regexp_e) and !version(address).nil?
  end

  def self.version(address)
    decoded = b58_decode(address)

    version = decoded[0, 1]
    checksum = decoded[-4, decoded.length]
    vh160 = decoded[0, decoded.length - 4]

    hashed = (Digest::SHA2.new << (Digest::SHA2.new << vh160).digest).digest

    hashed[0, 4] == checksum ? version[0] : nil
  end

  def self.regexp
    return /\b1[a-zA-HJ-NP-Z1-9]{26,34}\b/
  end


  def self.regexp_e
    return /^1[a-zA-HJ-NP-Z1-9]{26,34}$/
  end

  def self.b58_decode(value)
    long_value = 0
    index = 0
    result = ""
    result.force_encoding("BINARY")
    value.reverse.each_char do |c|
      begin
        long_value += B58Chars.index(c) * (B58Base ** index)
      rescue Exception => e
        return "INVALIDZZZZZZZ"
      end
      index += 1
    end

    while long_value >= 256 do
      div, mod = long_value.divmod 256
      result = mod.chr("BINARY") + result
      long_value = div
    end

    result = long_value.chr("BINARY") + result

    value.each_char do |c|
      if c=="1" 
        result = 0.chr("BINARY") + result
      else
        break
      end
    end

    result
  end

  

end


