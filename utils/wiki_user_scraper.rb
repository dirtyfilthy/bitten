require 'mechanize'
require './db.rb'
require 'pp'
agent=Mechanize.new
page=agent.get("https://en.bitcoin.it/w/index.php?title=Special:ListUsers&limit=500")
loop do
page.root.css("a").each do |link|
  site=link.attribute("href").to_s.strip
  next unless site=~/^\/wiki\/User:/
  site="https://en.bitcoin.it"+site
  label=link.text()
  address=Address.from_webpage(site).first
  puts "#{label} #{site} #{address}"
  unless address.nil?
    a=Address.find_or_create_by_base58hash address
    a.label=label
    a.notes=site
    a.save
  end
end
page=agent.page.link_with(:text=>"next 500").click
end
