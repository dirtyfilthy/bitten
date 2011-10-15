require 'mechanize'
require './db.rb'
require 'pp'


c=1
while(true) do
  agent=Mechanize.new
  url="http://www.bitcoinfeedback.com/viewuser.php?id=#{c}"
  puts url
  begin  
    page=agent.get(url)
  rescue Exception => e
    next
  end
  body=page.body
  begin
    name=body.scan(/<title>BitCoin Feedback and Reviews - (\w+)<\/title>/).first.first
  rescue Exception => e
    puts "failed"
    c=c+1
    next
  end
  addr=page.body.scan(Address.regexp).first
 if Address.valid_bitcoin_address?(addr)
    website=page.body.scan(/Website: <a href='(.*?)'>/).first.first rescue nil
    notes=url
    notes="#{website} (#{url})" if website and website.strip!=""
    
    puts "Found valid bitcoin address for #{name} -- #{addr} -- #{notes}"
    a=Address.find_or_create_by_base58hash addr
    a.label=name
    a.notes=url
    a.save
  end
  c+=1
end
