require 'mechanize'
require './db.rb'
require 'pp'


c=34250
while(true) do
  agent=Mechanize.new
  url="https://bitcointalk.org/index.php?action=profile;u=#{c}"
  puts url
  begin  
    page=agent.get(url)
  rescue Exception => e
    next
  end
  body=page.body
  begin
    name=body.scan(/<title>View the profile of (\w+)<\/title>/).first.first
  rescue Exception => e
    puts "failed"
    c=c+1
    next
  end
  addr=page.root.css(".signature").first.to_html.scan(Address.regexp).first
 if Address.valid_bitcoin_address?(addr)
    puts "Found valid bitcoin address for #{name} -- #{addr} -- url"
    a=Address.find_or_create_by_base58hash addr
    a.label=name
    a.notes=url
    a.save
  end
  c+=1
end
