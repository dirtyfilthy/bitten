require 'mechanize' 
require './db.rb'
require 'pp'
c=1
agent=Mechanize.new
index=1
while(true) 
url="http://www.google.com/codesearch/feeds/search?q=#{CGI::escape('[^a-zA-Z0-9]1[a-zA-HJ-NP-Z1-9]{26,34}[^a-zA-Z0-9] address')}&max-results=100&start-index=#{index}"

#begin
  puts "INDEX #{index} #{url}"
  page=agent.get(url)
#rescue Exception => e
#  next;
#end
xml=Nokogiri::XML(page.body)
xml.css("entry").each do |entry|
  uri=entry.css("gcs|package").first.attributes["uri"]
  entry.css("gcs|match").each do |m|
    addr=m.text().scan(Address.regexp).first
    if Address.valid_bitcoin_address?(addr)
      label=uri.text()
      if /^git:\/\/github\.com/.match(uri)
        label=uri.text().scan(/^git:\/\/github\.com\/(\w+)/).first.first rescue uri
      end
      a=Address.find_or_create_by_base58hash(addr)
      a.label=label if a.label==""
      a.notes+=uri.text()+"\n" if a.notes.scan(uri.text()).empty?
      puts "#{label} #{addr} #{uri}"
      a.save
    end
  end
end
index=index+100
end
