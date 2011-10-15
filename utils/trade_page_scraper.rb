require 'mechanize'
require './db.rb'

agent=Mechanize.new
page=agent.get("https://en.bitcoin.it/wiki/Trade")
from=736
c=0
page.root.css("a.external").each do |link|
  c=c+1
  if(c<from)
    next;
  end
  puts c
  site=link.attribute("href").to_s.strip
  address=Address.from_webpage(site).first
  label=link.text()
  puts "#{label} #{site} #{address}"
  unless address.nil?
    a=Address.find_or_create_by_base58hash address
    a.label=label
    a.notes=site
    a.save
  end
end
